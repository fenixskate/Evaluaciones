package automation.selectors;

import org.openqa.selenium.By;

/** Localizadores de LoginPage. No ejecuta acciones ni mantiene estado del navegador. */
public final class LoginSelectors {
    public static final By USERNAME = By.cssSelector("[data-test='username']");
    public static final By PASSWORD = By.cssSelector("[data-test='password']");
    public static final By SUBMIT = By.cssSelector("[data-test='login-button']");
    public static final By ERROR = By.cssSelector("[data-test='error']");

    private LoginSelectors() {}
}
