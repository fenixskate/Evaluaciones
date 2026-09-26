package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class ConfigManager {
    private static final Properties PROPERTIES = load();

    private ConfigManager() {}

    private static Properties load() {
        String environment = System.getProperty("env", "qa");
        if (!environment.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Nombre de entorno invalido: " + environment);
        }
        String resource = "config/" + environment + ".properties";
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("No se encontro " + resource);
            }
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar " + resource, e);
        }
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key.toUpperCase(Locale.ROOT).replace('.', '_'));
        }
        if (value == null) {
            value = PROPERTIES.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Configuracion requerida: " + key);
        }
        return value.trim();
    }

    public static int positiveInt(String key) {
        int value = Integer.parseInt(get(key));
        if (value <= 0) {
            throw new IllegalArgumentException(key + " debe ser mayor que cero");
        }
        return value;
    }

    public static boolean booleanValue(String key) {
        String value = get(key);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(key + " debe ser true o false");
        }
        return Boolean.parseBoolean(value);
    }
}
