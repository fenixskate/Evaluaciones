package steps;

import automation.actions.CheckoutActions;
import config.ConfigManager;
import automation.driver.DriverManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import org.junit.Assert;
import automation.pages.CheckoutPage;

public class CheckoutSteps {
    private CheckoutActions actions() {
        return new CheckoutActions(DriverManager.getDriver());
    }

    @Cuando("inicia el checkout")
    public void start() {
        actions().start();
    }

    @Cuando("completa los siguientes datos del comprador")
    public void completeBuyer(DataTable table) {
        var data = table.asMap(String.class, String.class);
        actions().completeBuyer(data.get("nombre"), data.get("apellido"), data.get("codigoPostal"));
    }

    @Entonces("el resumen muestra el siguiente producto e importes")
    public void verifySummary(DataTable table) {
        var data = table.asMap(String.class, String.class);
        CheckoutPage page = new CheckoutPage(DriverManager.getDriver());
        Assert.assertEquals(data.get("titulo"), page.title());
        Assert.assertEquals("Un solo producto en el resumen", 1, page.productCount());
        Assert.assertEquals(data.get("nombre"), page.productName());
        Assert.assertEquals(data.get("cantidad"), page.productQuantity());
        BigDecimal price = amount(data.get("precio"));
        BigDecimal subtotal = price.multiply(new BigDecimal(data.get("cantidad")));
        BigDecimal tax = subtotal.multiply(new BigDecimal(data.get("tasaImpuesto")))
                .setScale(2, RoundingMode.HALF_UP);
        assertAmount("Precio", price, page.productPrice());
        assertAmount("Subtotal", subtotal, page.subtotal());
        assertAmount("Impuesto", tax, page.tax());
        assertAmount("Total", subtotal.add(tax), page.total());
    }

    private BigDecimal amount(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\$(\\d+\\.\\d{2})").matcher(text);
        if (!matcher.find()) throw new IllegalArgumentException("Importe no reconocido: " + text);
        return new BigDecimal(matcher.group(1));
    }

    private void assertAmount(String label, BigDecimal expected, String actual) {
        Assert.assertEquals(label + ": esperado " + expected + ", obtenido " + actual,
                0, expected.compareTo(amount(actual)));
    }

    @Cuando("confirma la compra")
    public void confirm() {
        actions().confirm();
    }

    @Entonces("se muestra la siguiente confirmación de compra")
    public void verifyConfirmation(DataTable table) {
        var data = table.asMap(String.class, String.class);
        CheckoutPage page = new CheckoutPage(DriverManager.getDriver());
        Assert.assertEquals(data.get("titulo"), page.confirmationHeader());
        Assert.assertEquals(data.get("descripcion"), page.confirmationText());
        Assert.assertEquals(URI.create(ConfigManager.get("base.url"))
                        .resolve(data.get("ruta")),
                URI.create(DriverManager.getDriver().getCurrentUrl()));
    }
}
