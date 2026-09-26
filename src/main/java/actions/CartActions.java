package actions;

import config.ConfigManager;
import org.openqa.selenium.WebDriver;
import pages.CartPage;
import pages.HomePage;

public final class CartActions {
    private final HomePage home;
    private final CartPage cart;

    public CartActions(WebDriver driver) {
        home = new HomePage(driver);
        cart = new CartPage(driver);
    }

    public void addConfiguredProduct() {
        home.addProduct(ConfigManager.get("product.id"));
    }

    public void openCart() {
        home.openCart();
        cart.title();
    }

    public void prepareCart() {
        addConfiguredProduct();
        openCart();
    }

    public void removeConfiguredProduct() {
        cart.removeProduct(ConfigManager.get("product.id"));
    }
}
