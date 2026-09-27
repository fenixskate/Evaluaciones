package reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Genera evidencias desde resultados reales; no cambia el estado de los escenarios. */
public final class ScenarioPdfReporter implements ConcurrentEventListener {
    private final Map<UUID, ScenarioEvidence> active = new ConcurrentHashMap<>();
    private final Properties expected = new Properties();
    private final Path directory = createRunDirectory();

    private static Path createRunDirectory() {
        try {
            Path root = Path.of("output", "pdf");
            Files.createDirectories(root);
            // Reserva atomica: dos ejecuciones no comparten carpeta ni sobreescriben evidencias.
            for (int attempt = 0; attempt < 100; attempt++) {
                Path candidate = root.resolve(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("ddMMyy_HHmmss")));
                try {
                    return Files.createDirectory(candidate);
                } catch (FileAlreadyExistsException collision) {
                    Thread.sleep(100);
                }
            }
            throw new IOException("No se pudo reservar una carpeta para la ejecucion");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Reserva de carpeta interrumpida", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ScenarioPdfReporter() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/expected-results.properties")) {
            if (input == null) throw new IllegalStateException("Faltan resultados esperados");
            expected.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, event -> {
            try {
                Files.createDirectories(Path.of("target"));
                Files.writeString(Path.of("target", "current-run.txt"), directory.toString().replace('\\', '/'));
                Files.writeString(Path.of("target", "execution-timeline.csv"), "scenario,thread,start,end,status\n");
            } catch (IOException e) { throw new UncheckedIOException(e); }
        });
        publisher.registerHandlerFor(TestCaseStarted.class, event -> {
            ScenarioEvidence evidence = new ScenarioEvidence(event);
            active.put(event.getTestCase().getId(), evidence);
            System.out.println("[INICIO] " + evidence.thread + " | " + evidence.testCase.getName());
        });
        publisher.registerHandlerFor(TestStepStarted.class, event -> {
            if (event.getTestStep() instanceof PickleStepTestStep step) {
                ScenarioEvidence evidence = active.get(event.getTestCase().getId());
                StepEvidence item = new StepEvidence(step.getStep().getKeyword() + step.getStep().getText());
                evidence.steps.add(item);
            }
        });
        publisher.registerHandlerFor(TestStepFinished.class, event -> {
            ScenarioEvidence evidence = active.get(event.getTestCase().getId());
            if (event.getTestStep() instanceof PickleStepTestStep) {
                StepEvidence step = evidence.current();
                step.result = event.getResult();
                System.out.println("[PASO] " + step.name + " -> " + event.getResult().getStatus());
            } else if (event.getResult().getError() != null) {
                evidence.hookErrors.add(event.getResult().getError().toString());
            }
        });
        publisher.registerHandlerFor(EmbedEvent.class, event -> {
            ScenarioEvidence evidence = active.get(event.getTestCase().getId());
            if (evidence != null && evidence.current() != null && "image/png".equals(event.getMediaType())) {
                evidence.current().screenshot = event.getData();
            }
        });
        publisher.registerHandlerFor(TestCaseFinished.class, event -> {
            ScenarioEvidence evidence = active.remove(event.getTestCase().getId());
            writePdf(evidence, event.getResult());
            recordTiming(evidence, event);
        });
    }

    private synchronized void recordTiming(ScenarioEvidence evidence, TestCaseFinished event) {
        try {
            String name = evidence.testCase.getName().replace("\"", "\"\"");
            Files.writeString(Path.of("target", "execution-timeline.csv"),
                    "\"" + name + "\"," + evidence.thread + "," + evidence.started + ","
                            + event.getInstant() + "," + event.getResult().getStatus() + "\n",
                    StandardOpenOption.APPEND);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private void writePdf(ScenarioEvidence evidence, Result result) {
        try {
            Files.createDirectories(directory);
            String caseId = evidence.testCase.getTags().stream()
                    .filter(tag -> tag.matches("@CP[0-9]+")).findFirst().orElse("@ESCENARIO").substring(1);
            String fileId = caseId.startsWith("CP")
                    ? String.format(Locale.ROOT, "CP%03d", Integer.parseInt(caseId.substring(2))) : caseId;
            String scenarioName = evidence.testCase.getName()
                    .replaceAll("[<>:\"/\\\\|?*\\p{Cntrl}]", "_").replaceAll("[. ]+$", "");
            Path file = directory.resolve(fileId + "_" + scenarioName + ".pdf");
            try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {
                Document document = new Document(PageSize.A4, 42, 42, 42, 42);
                PdfWriter writer = PdfWriter.getInstance(document, output);
                writer.setPageEvent(new PdfPageEventHelper() {
                    @Override
                    public void onEndPage(PdfWriter writer, Document document) {
                        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                                new Phrase(caseId + " | Página " + writer.getPageNumber(),
                                        FontFactory.getFont(FontFactory.HELVETICA, 9)),
                                document.right(), 23, 0);
                    }
                });
                document.addTitle(caseId + " - " + evidence.testCase.getName());
                document.addAuthor("Framework QA - Módulo 1");
                document.open();
                paragraph(document, "SAUCEDEMO / MÓDULO 1", 11, Color.DARK_GRAY);
                paragraph(document, caseId + " - " + evidence.testCase.getName(), 22, new Color(20, 55, 85));
                paragraph(document, "Resultado: " + status(result.getStatus()), 16,
                        result.getStatus() == Status.PASSED ? new Color(0, 110, 75) : new Color(170, 45, 45));
                paragraph(document, "Inicio: " + evidence.started + "\nDuración: "
                        + String.format(Locale.ROOT, "%.2f s", result.getDuration().toMillis() / 1000.0)
                        + "\nPasos: " + evidence.steps.size(), 11, Color.DARK_GRAY);
                paragraph(document, "Resumen de ejecución", 15, Color.BLACK);
                for (int index = 0; index < evidence.steps.size(); index++) {
                    StepEvidence step = evidence.steps.get(index);
                    paragraph(document, (index + 1) + ". " + step.name + " ["
                            + (step.result == null ? "SIN RESULTADO" : status(step.result.getStatus())) + "]",
                            11, Color.BLACK);
                }
                for (String error : evidence.hookErrors) {
                    paragraph(document, "Error de preparación/cierre: " + error, 10, Color.RED);
                }
                paragraph(document, "Un paso aprobado indica que su implementación terminó sin error. "
                        + "Las validaciones comprobadas se describen en el resultado esperado de cada paso. "
                        + "Los pasos omitidos no validan el resultado esperado.", 10, Color.DARK_GRAY);
                for (int index = 0; index < evidence.steps.size(); index++) {
                    StepEvidence step = evidence.steps.get(index);
                    document.newPage();
                    paragraph(document, caseId + " / PASO " + (index + 1), 12, Color.DARK_GRAY);
                    paragraph(document, step.name, 18, new Color(20, 55, 85));
                    paragraph(document, "Resultado esperado", 12, Color.BLACK);
                    paragraph(document, expected.getProperty(caseId + "." + (index + 1),
                            "No definido: completar config/expected-results.properties."), 11, Color.DARK_GRAY);
                    paragraph(document, "Resultado obtenido: "
                            + (step.result == null ? "SIN RESULTADO" : status(step.result.getStatus())), 12, Color.BLACK);
                    if (step.result != null && step.result.getError() != null) {
                        paragraph(document, step.result.getError().toString(), 10, Color.RED);
                    }
                    if (step.screenshot != null) {
                        Image image = Image.getInstance(step.screenshot);
                        image.scaleToFit(document.right() - document.left(), 355);
                        image.setSpacingBefore(15);
                        document.add(image);
                        paragraph(document, "Evidencia capturada al finalizar el paso.", 9, Color.DARK_GRAY);
                    } else {
                        paragraph(document, "Sin captura disponible para este paso.", 10, Color.DARK_GRAY);
                    }
                }
                document.close();
            }
            System.out.println("[PDF] " + file.toAbsolutePath());
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del escenario", e);
        }
    }

    private static void paragraph(Document document, String text, int size, Color color) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, size, color));
        paragraph.setLeading(size * 1.35f);
        paragraph.setSpacingAfter(12);
        document.add(paragraph);
    }

    private static String status(Status status) {
        return switch (status) {
            case PASSED -> "APROBADO";
            case FAILED -> "FALLIDO";
            case SKIPPED -> "OMITIDO";
            case PENDING -> "PENDIENTE";
            case UNDEFINED -> "SIN IMPLEMENTAR";
            case AMBIGUOUS -> "AMBIGUO";
            case UNUSED -> "NO UTILIZADO";
        };
    }

    private static final class ScenarioEvidence {
        final TestCase testCase;
        final Instant started;
        final String thread = Thread.currentThread().getName();
        final java.util.List<StepEvidence> steps = new ArrayList<>();
        final java.util.List<String> hookErrors = new ArrayList<>();
        ScenarioEvidence(TestCaseStarted event) {
            testCase = event.getTestCase();
            started = event.getInstant();
        }
        StepEvidence current() {
            return steps.isEmpty() ? null : steps.get(steps.size() - 1);
        }
    }

    private static final class StepEvidence {
        final String name;
        Result result;
        byte[] screenshot;
        StepEvidence(String name) { this.name = name; }
    }
}
