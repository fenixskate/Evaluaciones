package config;

import org.junit.Test;
import java.net.URI;
import org.junit.Assert;

public class ConfigTest {
    @Test
    public void validateConfiguration() {
        URI uri = URI.create(ConfigManager.get("base.url"));
        Assert.assertTrue("La URL debe usar HTTP o HTTPS",
                "https".equals(uri.getScheme()) || "http".equals(uri.getScheme()));
        Assert.assertNotNull("La URL debe tener host", uri.getHost());
        Assert.assertTrue("Navegador soportado",
                java.util.Set.of("chrome", "firefox").contains(
                        ConfigManager.get("browser").toLowerCase(java.util.Locale.ROOT)));
        ConfigManager.booleanValue("headless");
        ConfigManager.positiveInt("timeout.explicit.seconds");
        ConfigManager.positiveInt("timeout.pageLoad.seconds");
    }
}
