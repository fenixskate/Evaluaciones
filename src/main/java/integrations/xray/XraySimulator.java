package integrations.xray;

import com.sun.net.httpserver.HttpServer;
import config.ConfigManager;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.openqa.selenium.json.Json;

/** Demostracion HTTP local del contrato Xray Server/DC; no modifica Jira real. */
public final class XraySimulator {
    private XraySimulator() {}

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Json json = new Json();
        Path run = Path.of(Files.readString(Path.of("target/current-run.txt")).trim());
        Path output = Path.of("output", "integrations", run.getFileName().toString());
        Files.createDirectories(output);
        List<Map<String, Object>> features = json.toType(
                Files.readString(Path.of("target/cucumber-report.json")), List.class);
        List<Map<String, String>> tests = new ArrayList<>();
        for (Map<String, Object> feature : features) {
            for (Map<String, Object> scenario : (List<Map<String, Object>>) feature.get("elements")) {
                if (!"scenario".equals(scenario.get("type"))) continue;
                String caseId = ((List<Map<String, Object>>) scenario.get("tags")).stream()
                        .map(tag -> tag.get("name").toString()).filter(tag -> tag.matches("@CP[0-9]+"))
                        .findFirst().orElseThrow().substring(1);
                List<String> statuses = new ArrayList<>();
                collectStatuses(scenario, statuses);
                String status = statuses.stream().anyMatch(s -> Set.of("failed", "ambiguous", "undefined", "pending").contains(s))
                        ? "FAIL" : !statuses.isEmpty() && statuses.stream().allMatch("passed"::equals) ? "PASS" : "TODO";
                tests.add(Map.of("testKey", ConfigManager.get("xray.case." + caseId),
                        "status", status, "comment", scenario.get("name").toString()));
            }
        }
        if (tests.isEmpty()) throw new IllegalStateException("No hay escenarios para importar");
        String payload = json.toJson(Map.of("testExecutionKey", ConfigManager.get("xray.execution.key"), "tests", tests));
        Files.writeString(output.resolve("request.json"), payload);
        String endpoint = ConfigManager.get("xray.mock.path");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(endpoint, exchange -> {
            int code = 200;
            String response;
            try {
                if (!"POST".equals(exchange.getRequestMethod())) throw new IllegalArgumentException("Se requiere POST");
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, Object> received = json.toType(body, Map.class);
                List<Map<String, String>> updates = (List<Map<String, String>>) received.get("tests");
                if (updates == null || updates.isEmpty() || updates.stream().anyMatch(test ->
                        test.get("testKey") == null || !Set.of("PASS", "FAIL", "TODO").contains(test.get("status")))) {
                    throw new IllegalArgumentException("Contrato de importacion invalido");
                }
                Files.writeString(output.resolve("received.json"), body);
                Files.writeString(output.resolve("jira-state-simulated.json"), json.toJson(updates));
                response = json.toJson(Map.of("simulated", true, "updated", updates.size(),
                        "testExecutionKey", received.get("testExecutionKey")));
            } catch (Exception error) {
                code = 400;
                response = json.toJson(Map.of("error", error.toString(), "simulated", true));
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(code, bytes.length);
            try (var body = exchange.getResponseBody()) { body.write(bytes); }
            exchange.close();
        });
        server.start();
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + endpoint);
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build(),
                    HttpResponse.BodyHandlers.ofString());
            Files.writeString(output.resolve("response.json"), response.body());
            Files.writeString(output.resolve("http.txt"), "POST " + uri + "\nHTTP " + response.statusCode() + "\nSIMULACION LOCAL\n");
            if (response.statusCode() != 200) throw new IllegalStateException("Importacion rechazada: " + response.body());
            System.out.println("[XRAY SIMULADO] HTTP 200; casos actualizados: " + tests.size() + "; " + output);
        } finally { server.stop(0); }
    }

    private static void collectStatuses(Object value, List<String> statuses) {
        if (value instanceof Map<?, ?> map) {
            if (map.get("result") instanceof Map<?, ?> result && result.get("status") instanceof String status) {
                statuses.add(status);
            }
            map.values().forEach(child -> collectStatuses(child, statuses));
        } else if (value instanceof List<?> list) {
            list.forEach(child -> collectStatuses(child, statuses));
        }
    }
}
