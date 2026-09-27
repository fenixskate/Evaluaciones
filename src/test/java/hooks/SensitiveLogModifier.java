package hooks;
import com.intuit.karate.http.HttpLogModifier;
import config.ConfigManager;
public class SensitiveLogModifier implements HttpLogModifier {
    @Override public boolean enableForUri(String uri) { return true; }
    @Override public String uri(String uri) { return uri; }
    @Override public String header(String name, String value) {
        return name.equalsIgnoreCase("Authorization") || name.equalsIgnoreCase("Cookie")
                || name.equalsIgnoreCase("Set-Cookie") || name.equalsIgnoreCase(ConfigManager.get("auth.api.key.header"))
                ? "[REDACTED]" : value;
    }
    @Override public String request(String uri, String body) { return body; }
    @Override public String response(String uri, String body) { return body; }
}
