package automation.actions;

import org.openqa.selenium.WebDriver;
import automation.pages.CartPage;
import automation.pages.HomePage;

public final class CartActions {
    private final HomePage home;
    private final CartPage cart;

    public CartActions(WebDriver driver) {
        home = new HomePage(driver);
        cart = new CartPage(driver);
    }

    public void addProduct(String productId) {
        home.addProduct(productId);
    }

    public void openCart() {
        home.openCart();
        cart.title();
    }

    public void prepareCart(String productId) {
        addProduct(productId);
        openCart();
    }

    public void removeProduct(String productId) {
        cart.removeProduct(productId);
    }
}
