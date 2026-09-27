package steps;

import automation.actions.CartActions;
import io.cucumber.datatable.DataTable;
import automation.driver.DriverManager;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.junit.Assert;
import automation.pages.CartPage;

public class CartSteps {
    private CartActions actions() {
        return new CartActions(DriverManager.getDriver());
    }

    @Cuando("agrega el producto {string} desde el catálogo")
    public void addProduct(String productId) {
        actions().addProduct(productId);
    }

    @Cuando("navega al carrito")
    public void openCart() {
        actions().openCart();
    }

    @Dado("tiene el siguiente producto en el carrito")
    public void prepareCart(DataTable table) {
        actions().prepareCart(table.asMap(String.class, String.class).get("id"));
        verifyProduct(table);
    }

    @Entonces("el carrito contiene el siguiente producto")
    public void verifyProduct(DataTable table) {
        var data = table.asMap(String.class, String.class);
        CartPage cart = new CartPage(DriverManager.getDriver());
        String id = data.get("id");
        Assert.assertEquals("Titulo del carrito", data.get("titulo"), cart.title());
        Assert.assertEquals("Producto", data.get("nombre"), cart.productName(id));
        Assert.assertEquals("Precio", data.get("precio"), cart.productPrice(id));
        Assert.assertEquals("Cantidad", data.get("cantidad"), cart.productQuantity(id));
    }

    @Cuando("elimina el producto {string} desde el carrito")
    public void removeProduct(String productId) {
        actions().removeProduct(productId);
    }

    @Entonces("el carrito queda vacío")
    public void verifyEmpty() {
        Assert.assertTrue("No debe haber productos ni contador del carrito",
                new CartPage(DriverManager.getDriver()).isEmpty());
    }
}
