package automation.actions;

import org.openqa.selenium.WebDriver;
import automation.pages.LoginPage;

public final class LoginActions {
    private final LoginPage loginPage;

    public LoginActions(WebDriver driver) {
        loginPage = new LoginPage(driver);
    }

    public void openLogin() {
        loginPage.open();
    }

    public void signIn(String username, String password) {
        loginPage.signIn(username, password);
    }
}
