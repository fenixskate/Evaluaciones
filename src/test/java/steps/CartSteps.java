package steps;

import actions.CartActions;
import config.ConfigManager;
import driver.DriverManager;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.junit.Assert;
import pages.CartPage;

public class CartSteps {
    private CartActions actions() {
        return new CartActions(DriverManager.getDriver());
    }

    @Cuando("agrega el producto configurado desde el catálogo")
    public void addProduct() {
        actions().addConfiguredProduct();
    }

    @Cuando("navega al carrito")
    public void openCart() {
        actions().openCart();
    }

    @Dado("tiene el producto configurado en el carrito")
    public void prepareCart() {
        actions().prepareCart();
        verifyProduct();
    }

    @Entonces("el carrito contiene el producto seleccionado con su precio y cantidad")
    public void verifyProduct() {
        CartPage cart = new CartPage(DriverManager.getDriver());
        String id = ConfigManager.get("product.id");
        Assert.assertEquals("Titulo del carrito", ConfigManager.get("expected.cart.title"), cart.title());
        Assert.assertEquals("Producto", ConfigManager.get("product.name"), cart.productName(id));
        Assert.assertEquals("Precio", ConfigManager.get("product.price"), cart.productPrice(id));
        Assert.assertEquals("Cantidad", ConfigManager.get("product.quantity"), cart.productQuantity(id));
    }

    @Cuando("elimina el producto desde el carrito")
    public void removeProduct() {
        actions().removeConfiguredProduct();
    }

    @Entonces("el carrito queda vacío")
    public void verifyEmpty() {
        Assert.assertTrue("No debe haber productos ni contador del carrito",
                new CartPage(DriverManager.getDriver()).isEmpty());
    }
}
