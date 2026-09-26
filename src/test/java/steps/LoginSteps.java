package steps;

import actions.LoginActions;
import config.ConfigManager;
import driver.DriverManager;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.net.URI;
import org.junit.Assert;
import pages.HomePage;
import pages.LoginPage;

public class LoginSteps {
    // Se crean al ejecutar el paso, despues del hook que inicia el navegador.
    private LoginActions actions() {
        return new LoginActions(DriverManager.getDriver());
    }

    @Dado("que el usuario está en la página de login")
    public void openLogin() {
        actions().openLogin();
    }

    @Cuando("inicia sesión con el usuario válido configurado")
    public void signInValid() {
        actions().signInAsValidUser();
    }

    @Dado("que el usuario válido ha iniciado sesión")
    public void authenticatedUser() {
        openLogin();
        signInValid();
        verifyCatalog();
    }

    @Cuando("inicia sesión con el usuario bloqueado configurado")
    public void signInBlocked() {
        actions().signInAsBlockedUser();
    }

    @Entonces("se muestra el catálogo de productos")
    public void verifyCatalog() {
        HomePage home = new HomePage(DriverManager.getDriver());
        Assert.assertEquals("Titulo del catalogo", ConfigManager.get("expected.catalog.title"), home.title());
        Assert.assertTrue("El catalogo debe mostrar productos", home.hasProducts());
        URI expected = URI.create(ConfigManager.get("base.url")).resolve(ConfigManager.get("expected.catalog.path"));
        Assert.assertEquals("Destino del login", expected, URI.create(DriverManager.getDriver().getCurrentUrl()));
    }

    @Entonces("se muestra el mensaje de usuario bloqueado")
    public void verifyBlockedMessage() {
        Assert.assertEquals("Mensaje de acceso bloqueado", ConfigManager.get("expected.login.blocked"),
                new LoginPage(DriverManager.getDriver()).errorMessage());
    }

    @Entonces("permanece en la página de login")
    public void verifyLoginPage() {
        Assert.assertTrue("El formulario debe permanecer visible",
                new LoginPage(DriverManager.getDriver()).isDisplayed());
        Assert.assertEquals("El usuario no debe salir del login",
                URI.create(ConfigManager.get("base.url")),
                URI.create(DriverManager.getDriver().getCurrentUrl()));
    }
}
