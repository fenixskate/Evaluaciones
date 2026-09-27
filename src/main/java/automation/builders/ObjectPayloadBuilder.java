package automation.builders;

import automation.utils.JsonResources;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;

/**
 * Builder de payloads para el recurso {@code /objects}.
 * Cada instancia trabaja sobre una copia independiente del JSON de datos.
 */
public final class ObjectPayloadBuilder {
    private final ObjectNode payload;
    /** Carga una plantilla por nombre desde {@code data/objects.json}. */
    public ObjectPayloadBuilder(String template) {
        JsonNode node = JsonResources.read("data/objects.json").required(template);
        if (!node.isObject()) throw new IllegalArgumentException("Se requiere un objeto JSON");
        payload = ((ObjectNode) node).deepCopy();
    }
    /** Agrega un UUID al nombre para evitar colisiones entre ejecuciones. */
    public ObjectPayloadBuilder uniqueName() {
        return name(payload.required("name").asText() + " - " + UUID.randomUUID());
    }
    /** Reemplaza el nombre del recurso y permite continuar encadenando operaciones. */
    public ObjectPayloadBuilder name(String name) { payload.put("name", name); return this; }
    /** Valida los campos mínimos y devuelve una copia lista para enviar. */
    public ObjectNode build() {
        if (!payload.hasNonNull("name") || !payload.get("name").isTextual() || payload.get("name").asText().isBlank())
            throw new IllegalArgumentException("El payload requiere name no vacio");
        return payload.deepCopy();
    }
}
