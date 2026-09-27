package hooks;

import automation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import automation.utils.ScreenshotUtils;

public class Hooks {
    @Before("@web")
    public void startBrowser(Scenario scenario) {
        scenario.log("Inicio: " + scenario.getName());
        DriverManager.start();
    }

    @AfterStep("@web")
    public void attachScreenshot(Scenario scenario) {
        if (!DriverManager.isStarted()) return;
        if (scenario.isFailed()) {
            try {
                scenario.log("Consola del navegador: " + DriverManager.getDriver().manage().logs().get("browser").getAll());
            } catch (RuntimeException unavailable) {
                scenario.log("Consola no disponible: " + unavailable.getMessage());
            }
        }
        try {
            scenario.attach(ScreenshotUtils.capture(DriverManager.getDriver()),
                    "image/png", scenario.isFailed() ? "Paso fallido" : "Paso ejecutado");
        } catch (RuntimeException e) {
            scenario.log("No se pudo capturar evidencia: " + e.getMessage());
        }
    }

    @After("@web")
    public void closeBrowser() {
        DriverManager.quit();
    }
}
