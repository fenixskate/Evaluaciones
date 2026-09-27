package automation.auth;

import automation.utils.JsonResources;
import config.ConfigManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

/**
 * Proveedor centralizado de autenticación para las peticiones de Karate.
 * Soporta peticiones públicas, Bearer, API Key y OAuth2 client credentials.
 * Los valores sensibles se leen desde configuración externa y nunca se registran.
 */
public final class AuthProvider {
    private String token;
    private Instant expiresAt = Instant.EPOCH;
    /** Devuelve los headers de autenticación correspondientes al entorno activo. */
    public Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        switch (ConfigManager.get("auth.mode").toLowerCase(Locale.ROOT)) {
            case "none" -> { }
            case "bearer" -> headers.put("Authorization", "Bearer " + ConfigManager.get("auth.bearer.token"));
            case "api-key" -> headers.put(ConfigManager.get("auth.api.key.header"), ConfigManager.get("auth.api.key"));
            case "oauth2" -> headers.put("Authorization", "Bearer " + oauthToken());
            default -> throw new IllegalArgumentException("auth.mode: none, bearer, api-key u oauth2");
        }
        return headers;
    }
    /** Obtiene y almacena temporalmente un token OAuth2 hasta que se acerque su expiración. */
    private synchronized String oauthToken() {
        if (token != null && Instant.now().isBefore(expiresAt)) return token;
        Duration timeout = Duration.ofMillis(ConfigManager.positiveInt("http.timeout.ms"));
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build()) {
            String credentials = encode(ConfigManager.get("auth.oauth.client.id")) + ":" + encode(ConfigManager.get("auth.oauth.client.secret"));
            var request = HttpRequest.newBuilder(URI.create(ConfigManager.get("auth.oauth.token.url"))).timeout(timeout)
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=" + encode(ConfigManager.get("auth.oauth.grant.type")))).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new IllegalStateException("OAuth HTTP " + response.statusCode());
            var body = JsonResources.MAPPER.readTree(response.body());
            String value = body.path("access_token").asText();
            long ttl = body.path("expires_in").asLong();
            if (value.isBlank() || ttl <= 0) throw new IllegalStateException("OAuth sin token o expiracion valida");
            token = value;
            expiresAt = Instant.now().plusSeconds(Math.max(0, ttl - ConfigManager.positiveInt("auth.oauth.expiry.margin.seconds")));
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); throw new IllegalStateException("OAuth interrumpido");
        } catch (java.io.IOException e) { throw new IllegalStateException("Fallo de transporte OAuth: " + e.getClass().getSimpleName()); }
    }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
