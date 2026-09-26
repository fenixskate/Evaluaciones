package utils;

import config.ConfigManager;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class WaitUtils {
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigManager.positiveInt("timeout.explicit.seconds")));
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void absent(By locator) {
        wait.until(ExpectedConditions.numberOfElementsToBe(locator, 0));
    }

    /** Solo para transiciones idempotentes cuyo destino confirma la accion. */
    public void clickUntilVisible(By source, By destination) {
        boolean[] clicked = {false};
        wait.until(driver -> {
            if (driver.findElements(destination).stream().anyMatch(WebElement::isDisplayed)) return true;
            try {
                for (WebElement element : driver.findElements(source)) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        if (!clicked[0]) {
                            element.click();
                            clicked[0] = true;
                        } else {
                            System.out.println("[REINTENTO] Clic sin transicion confirmada: " + source);
                            element.click();
                        }
                        break;
                    }
                }
            } catch (org.openqa.selenium.StaleElementReferenceException
                    | org.openqa.selenium.ElementClickInterceptedException transientError) {
                // El DOM esta cambiando; se relocaliza en el siguiente sondeo.
            }
            return false;
        });
    }
}
