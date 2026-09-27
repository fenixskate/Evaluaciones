package automation.driver;

import config.ConfigManager;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {}

    public static void start() {
        if (isStarted()) {
            throw new IllegalStateException("Ya existe un navegador para este escenario");
        }
        boolean headless = ConfigManager.booleanValue("headless");
        String browser = ConfigManager.get("browser").toLowerCase(Locale.ROOT);
        WebDriver driver = switch (browser) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();
                org.openqa.selenium.logging.LoggingPreferences logs = new org.openqa.selenium.logging.LoggingPreferences();
                logs.enable(org.openqa.selenium.logging.LogType.BROWSER, java.util.logging.Level.ALL);
                options.setCapability("goog:loggingPrefs", logs);
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1440,900");
                yield new ChromeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                yield new FirefoxDriver(options);
            }
            default -> throw new IllegalArgumentException("Navegador no soportado: " + browser);
        };
        DRIVER.set(driver);
        try {
            driver.manage().timeouts().pageLoadTimeout(
                    Duration.ofSeconds(ConfigManager.positiveInt("timeout.pageLoad.seconds")));
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1440, 900));
        } catch (RuntimeException e) {
            try { quit(); } catch (RuntimeException cleanup) { e.addSuppressed(cleanup); }
            throw e;
        }
    }

    public static boolean isStarted() {
        return DRIVER.get() != null;
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) throw new IllegalStateException("El navegador no esta iniciado");
        return driver;
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) driver.quit();
        } finally {
            DRIVER.remove();
        }
    }
}
