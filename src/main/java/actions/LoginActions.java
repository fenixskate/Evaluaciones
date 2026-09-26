package actions;

import config.ConfigManager;
import org.openqa.selenium.WebDriver;
import pages.LoginPage;

public final class LoginActions {
    private final LoginPage loginPage;

    public LoginActions(WebDriver driver) {
        loginPage = new LoginPage(driver);
    }

    public void openLogin() {
        loginPage.open();
    }

    public void signInAsValidUser() {
        loginPage.signIn(ConfigManager.get("user.valid.username"), ConfigManager.get("user.password"));
    }

    public void signInAsBlockedUser() {
        loginPage.signIn(ConfigManager.get("user.blocked.username"), ConfigManager.get("user.password"));
    }
}
