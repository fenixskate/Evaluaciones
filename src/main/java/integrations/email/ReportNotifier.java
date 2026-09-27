package integrations.email;

import config.ConfigManager;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.*;

/** Genera un correo .eml simulado con el reporte HTML/JSON adjunto; no conecta a SMTP. */
public final class ReportNotifier {
    private ReportNotifier() {}

    /** Busca el reporte actual y crea el borrador de correo en {@code output/email}. */
    public static void main(String[] args) throws Exception {
        Path report = args.length > 0 ? Path.of(args[0]) : latestReport();
        if (!Files.exists(report)) throw new IllegalArgumentException("No existe el reporte: " + report);
        Path attachment = Files.isDirectory(report) ? zip(report) : report;
        Path emailDir = Path.of("output", "email");
        Files.createDirectories(emailDir);
        Path eml = emailDir.resolve("karate-report.eml");
        String body = "From: " + ConfigManager.get("report.email.from") + "\r\n"
                + "To: " + ConfigManager.get("report.email.to") + "\r\n"
                + "Subject: [SIMULADO] " + ConfigManager.get("report.email.subject") + "\r\n\r\n"
                + ConfigManager.get("report.email.message") + "\r\n"
                + "Adjunto: " + attachment.toAbsolutePath() + "\r\n"
                + "Modo: simulación local; no se envió por SMTP.\r\n";
        Files.writeString(eml, body);
        System.out.println("[EMAIL SIMULADO] " + eml.toAbsolutePath());
    }

    private static Path latestReport() throws Exception {
        Path root = Path.of("output", "reports");
        try (Stream<Path> folders = Files.list(root)) {
            return folders.filter(Files::isDirectory).max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                    .orElseThrow(() -> new IllegalArgumentException("No existen reportes en " + root));
        }
    }

    private static Path zip(Path folder) throws Exception {
        Path zip = folder.resolveSibling("karate-report.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip));
             Stream<Path> files = Files.walk(folder)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                out.putNextEntry(new ZipEntry(folder.relativize(file).toString().replace('\\', '/')));
                Files.copy(file, out);
                out.closeEntry();
            }
        }
        return zip;
    }
}
