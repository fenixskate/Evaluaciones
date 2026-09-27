package automation.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

/** Utilidad para leer archivos JSON empaquetados dentro del classpath de pruebas. */
public final class JsonResources {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private JsonResources() {}
    /** Lee y parsea un recurso JSON; falla con un mensaje claro si no existe o es inválido. */
    public static JsonNode read(String path) {
        try (InputStream input = JsonResources.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IllegalArgumentException("Recurso inexistente: " + path);
            return MAPPER.readTree(input);
        } catch (java.io.IOException e) { throw new IllegalStateException("JSON invalido: " + path, e); }
    }
}
