package automation.pages;

import config.ConfigManager;
import automation.selectors.LoginSelectors;
import org.openqa.selenium.WebDriver;
import automation.utils.WaitUtils;

public final class LoginPage {
    private final WebDriver driver;
    private final WaitUtils wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    public void open() {
        driver.get(ConfigManager.get("base.url"));
        wait.visible(LoginSelectors.USERNAME);
    }

    public void signIn(String user, String secret) {
        wait.visible(LoginSelectors.USERNAME).clear();
        wait.visible(LoginSelectors.USERNAME).sendKeys(user);
        wait.visible(LoginSelectors.PASSWORD).clear();
        wait.visible(LoginSelectors.PASSWORD).sendKeys(secret);
        wait.clickable(LoginSelectors.SUBMIT).click();
    }

    public String errorMessage() {
        return wait.visible(LoginSelectors.ERROR).getText();
    }

    public boolean isDisplayed() {
        return wait.visible(LoginSelectors.USERNAME).isDisplayed()
                && wait.visible(LoginSelectors.PASSWORD).isDisplayed()
                && wait.clickable(LoginSelectors.SUBMIT).isDisplayed();
    }
}
