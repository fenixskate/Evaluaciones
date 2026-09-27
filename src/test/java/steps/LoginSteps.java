package steps;

import automation.actions.LoginActions;
import config.ConfigManager;
import automation.driver.DriverManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.net.URI;
import org.junit.Assert;
import automation.pages.HomePage;
import automation.pages.LoginPage;

public class LoginSteps {
    // Se crean al ejecutar el paso, despues del hook que inicia el navegador.
    private LoginActions actions() {
        return new LoginActions(DriverManager.getDriver());
    }

    @Dado("que el usuario está en la página de login")
    public void openLogin() {
        actions().openLogin();
    }

    @Cuando("inicia sesión con usuario {string} y contraseña {string}")
    public void signIn(String username, String password) {
        actions().signIn(username, password);
    }

    @Dado("que el usuario ha iniciado sesión con los siguientes datos")
    public void authenticatedUser(DataTable table) {
        var data = table.asMap(String.class, String.class);
        openLogin();
        signIn(data.get("usuario"), data.get("contraseña"));
        verifyCatalog(data.get("titulo"), data.get("ruta"));
    }

    @Entonces("se muestra el catálogo {string} en {string}")
    public void verifyCatalog(String title, String path) {
        HomePage home = new HomePage(DriverManager.getDriver());
        Assert.assertEquals("Titulo del catalogo", title, home.title());
        Assert.assertTrue("El catalogo debe mostrar productos", home.hasProducts());
        URI expected = URI.create(ConfigManager.get("base.url")).resolve(path);
        Assert.assertEquals("Destino del login", expected, URI.create(DriverManager.getDriver().getCurrentUrl()));
    }

    @Entonces("se muestra el mensaje de bloqueo {string}")
    public void verifyBlockedMessage(String message) {
        Assert.assertEquals("Mensaje de acceso bloqueado", message,
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
