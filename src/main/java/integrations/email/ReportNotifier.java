package integrations.email;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import config.ConfigManager;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/** Crea un correo local con adjunto. No realiza conexiones SMTP. */
public final class ReportNotifier {
    private ReportNotifier() {}

    public static void main(String[] args) throws Exception {
        Path attachment = args.length > 0 ? Path.of(args[0]) : latestEvidence();
        if (!Files.exists(attachment)) throw new IllegalArgumentException("Reporte no encontrado: " + attachment);
        String message = "SIMULACION LOCAL: este correo no fue enviado.\n"
                + "Se adjuntan las evidencias de la carpeta " + attachment.getFileName()
                + ". Consulte los resultados en cada PDF.";
        simulateEmail(attachment, message);
    }

    private static Path latestEvidence() throws Exception {
        Path root = Path.of("output", "pdf");
        try (Stream<Path> folders = Files.list(root)) {
            return folders.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().matches("\\d{6}_\\d{6}"))
                    .max(Comparator.comparing(path -> LocalDateTime.parse(path.getFileName().toString(),
                            DateTimeFormatter.ofPattern("ddMMyy_HHmmss"))))
                    .orElseThrow(() -> new IllegalArgumentException("No hay evidencias en output/pdf"));
        }
    }

    private static void simulateEmail(Path evidence, String message) throws Exception {
        Path attachment = evidence;
        if (Files.isDirectory(evidence)) attachment = zip(evidence);
        Properties props = new Properties();
        Session session = Session.getInstance(props);
        MimeMessage mail = new MimeMessage(session);
        mail.setFrom(new InternetAddress("automation@example.invalid"));
        mail.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ConfigManager.get("report.email.to")));
        mail.setSubject("[SIMULADO] " + ConfigManager.get("report.email.subject"), "UTF-8");
        mail.setSentDate(new java.util.Date());
        mail.setHeader("X-Unsent", "1");
        MimeBodyPart body = new MimeBodyPart(); body.setText(message, "UTF-8");
        MimeBodyPart file = new MimeBodyPart(); file.attachFile(attachment.toFile());
        MimeMultipart multipart = new MimeMultipart(); multipart.addBodyPart(body); multipart.addBodyPart(file);
        mail.setContent(multipart);
        mail.saveChanges();
        Path drafts = Path.of("output", "email");
        Files.createDirectories(drafts);
        Path draft = Files.createTempFile(drafts, evidence.getFileName() + "_", ".eml");
        try (java.io.OutputStream output = Files.newOutputStream(draft)) {
            mail.writeTo(output);
        }
        System.out.println("[EMAIL SIMULADO] No enviado. Destinatario: " + ConfigManager.get("report.email.to"));
        System.out.println("[ADJUNTO] " + attachment.toAbsolutePath());
        System.out.println("[CORREO] " + draft.toAbsolutePath());
    }

    private static Path zip(Path folder) throws Exception {
        java.util.List<Path> pdfs;
        try (Stream<Path> paths = Files.walk(folder)) {
            pdfs = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf"))
                    .sorted().toList();
        }
        if (pdfs.isEmpty()) throw new IllegalArgumentException("La carpeta no contiene evidencias PDF: " + folder);
        Path zip = folder.getParent().resolve(folder.getFileName() + ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (Path path : pdfs) {
                out.putNextEntry(new ZipEntry(folder.relativize(path).toString().replace('\\', '/')));
                Files.copy(path, out); out.closeEntry();
            }
        }
        return zip;
    }

}
