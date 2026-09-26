package steps;

import actions.CheckoutActions;
import config.ConfigManager;
import driver.DriverManager;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import org.junit.Assert;
import pages.CheckoutPage;

public class CheckoutSteps {
    private CheckoutActions actions() {
        return new CheckoutActions(DriverManager.getDriver());
    }

    @Cuando("inicia el checkout")
    public void start() {
        actions().start();
    }

    @Cuando("completa los datos del comprador configurado")
    public void completeBuyer() {
        actions().completeBuyer();
    }

    @Entonces("el resumen muestra el producto y los importes correctos")
    public void verifySummary() {
        CheckoutPage page = new CheckoutPage(DriverManager.getDriver());
        Assert.assertEquals(ConfigManager.get("expected.checkout.title"), page.title());
        Assert.assertEquals("Un solo producto en el resumen", 1, page.productCount());
        Assert.assertEquals(ConfigManager.get("product.name"), page.productName());
        Assert.assertEquals(ConfigManager.get("product.quantity"), page.productQuantity());
        BigDecimal price = amount(ConfigManager.get("product.price"));
        BigDecimal subtotal = price.multiply(new BigDecimal(ConfigManager.get("product.quantity")));
        BigDecimal tax = subtotal.multiply(new BigDecimal(ConfigManager.get("checkout.tax.rate")))
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

    @Entonces("se muestra la confirmación de compra exitosa")
    public void verifyConfirmation() {
        CheckoutPage page = new CheckoutPage(DriverManager.getDriver());
        Assert.assertEquals(ConfigManager.get("expected.checkout.complete"), page.confirmationHeader());
        Assert.assertEquals(ConfigManager.get("expected.checkout.description"), page.confirmationText());
        Assert.assertEquals(URI.create(ConfigManager.get("base.url"))
                        .resolve(ConfigManager.get("expected.checkout.path")),
                URI.create(DriverManager.getDriver().getCurrentUrl()));
    }
}
