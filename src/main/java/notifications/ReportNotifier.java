package notifications;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import config.ConfigManager;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/** Notificador desacoplado: simula por defecto y deja el punto de integracion real. */
public final class ReportNotifier {
    private ReportNotifier() {}

    public static void main(String[] args) throws Exception {
        String channel = value("REPORT_CHANNEL", "slack");
        String mode = value("REPORT_MODE", "simulated");
        Path attachment = args.length > 0 ? Path.of(args[0]) : latestEvidence();
        if (!Files.exists(attachment)) throw new IllegalArgumentException("Reporte no encontrado: " + attachment);
        String destination = value("REPORT_DESTINATION", "qa-automation");
        String message = "Suite modulo 1 finalizada: reporte adjunto (" + attachment + ")";
        if (emailEnabled()) {
            sendEmail(attachment, message);
            return;
        }
        if ("simulated".equalsIgnoreCase(mode)) {
            System.out.printf("[NOTIFICACION SIMULADA] canal=%s destino=%s fecha=%s%n%s%n",
                    channel, destination, LocalDateTime.now(), message);
            return;
        }
        if ("slack".equalsIgnoreCase(channel)) {
            System.out.println("[NOTIFICACION] Configure REPORT_WEBHOOK_URL y conecte un cliente HTTP para Slack.");
        } else if ("email".equalsIgnoreCase(channel)) {
            System.out.println("[NOTIFICACION] Configure SMTP_HOST, SMTP_PORT, SMTP_USER y SMTP_PASSWORD para correo.");
        } else throw new IllegalArgumentException("REPORT_CHANNEL debe ser slack o email");
        System.out.println("Mensaje: " + message);
    }

    private static Path latestEvidence() throws Exception {
        Path root = Path.of("output", "pdf");
        try (Stream<Path> folders = Files.list(root)) {
            return folders.filter(Files::isDirectory).max(Path::compareTo)
                    .orElseThrow(() -> new IllegalArgumentException("No hay evidencias en output/pdf"));
        }
    }

    private static void sendEmail(Path evidence, String message) throws Exception {
        String user = requiredEnv("SMTP_USER");
        String password = requiredEnv("SMTP_PASSWORD");
        Path attachment = evidence;
        if (Files.isDirectory(evidence)) attachment = zip(evidence);
        Properties props = new Properties();
        props.put("mail.smtp.host", requiredEnv("SMTP_HOST"));
        props.put("mail.smtp.port", value("SMTP_PORT", "587"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props);
        MimeMessage mail = new MimeMessage(session);
        mail.setFrom(new InternetAddress(user));
        mail.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ConfigManager.get("report.email.to")));
        mail.setSubject(ConfigManager.get("report.email.subject"), "UTF-8");
        MimeBodyPart body = new MimeBodyPart(); body.setText(message, "UTF-8");
        MimeBodyPart file = new MimeBodyPart(); file.attachFile(attachment.toFile());
        MimeMultipart multipart = new MimeMultipart(); multipart.addBodyPart(body); multipart.addBodyPart(file);
        mail.setContent(multipart);
        Transport.send(mail, user, password);
        System.out.println("[EMAIL] Enviado a " + ConfigManager.get("report.email.to") + " con " + attachment);
    }

    private static boolean emailEnabled() {
        String configured = System.getProperty("report.email.enabled");
        if (configured == null) configured = System.getenv("REPORT_EMAIL_ENABLED");
        if (configured == null) {
            try { configured = ConfigManager.get("report.email.enabled"); }
            catch (IllegalArgumentException missing) { configured = "false"; }
        }
        return Boolean.parseBoolean(configured);
    }

    private static Path zip(Path folder) throws Exception {
        Path zip = folder.getParent().resolve(folder.getFileName() + ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip)); Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                out.putNextEntry(new ZipEntry(folder.relativize(path).toString().replace('\\', '/')));
                Files.copy(path, out); out.closeEntry();
            }
        }
        return zip;
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Falta variable " + key);
        return value;
    }

    private static String value(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
