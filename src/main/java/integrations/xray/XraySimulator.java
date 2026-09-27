package integrations.xray;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import config.ConfigManager;
import java.nio.file.*;
import java.util.*;

/** Simula la actualización de casos Xray leyendo el reporte JSON de Karate; no llama Jira real. */
public final class XraySimulator {
    private XraySimulator() {}

    /** Convierte cada escenario con tag CPxx a un estado PASS/FAIL y guarda la petición simulada. */
    public static void main(String[] args) throws Exception {
        Path report = latestReport().resolve("features.objects.json");
        if (!Files.exists(report)) throw new IllegalArgumentException("No existe reporte JSON: " + report);
        JsonNode root = new ObjectMapper().readTree(report.toFile());
        List<Map<String, Object>> tests = new ArrayList<>();
        for (JsonNode feature : root) for (JsonNode scenario : feature.path("elements")) {
            if (!"scenario".equals(scenario.path("type").asText())) continue;
            String caseId = "";
            for (JsonNode tag : scenario.path("tags")) if (tag.path("name").asText().matches("@CP[0-9]+")) caseId = tag.path("name").asText().substring(1);
            if (caseId.isBlank()) continue;
            boolean failed = false;
            for (JsonNode step : scenario.path("steps")) if (!"passed".equals(step.path("result").path("status").asText())) failed = true;
            tests.add(Map.of("testKey", ConfigManager.get("xray.case." + caseId), "status", failed ? "FAIL" : "PASS", "scenario", scenario.path("name").asText()));
        }
        Path dir = Path.of("output", "integrations"); Files.createDirectories(dir);
        ObjectNode payload = new ObjectMapper().createObjectNode();
        payload.put("simulated", true); payload.put("testExecutionKey", ConfigManager.get("xray.execution.key"));
        payload.set("tests", new ObjectMapper().valueToTree(tests));
        Files.writeString(dir.resolve("xray-request.json"), payload.toPrettyString());
        Files.writeString(dir.resolve("xray-response.json"), "{\"simulated\":true,\"updated\":" + tests.size() + "}");
        System.out.println("[XRAY SIMULADO] casos preparados: " + tests.size());
    }

    private static Path latestReport() throws Exception {
        Path root = Path.of("output", "reports");
        try (var folders = Files.list(root)) {
            return folders.filter(Files::isDirectory).max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                    .orElseThrow(() -> new IllegalArgumentException("No existen reportes en " + root));
        }
    }
}
