package selectors;

import org.openqa.selenium.By;

/** Localizadores de HomePage. No ejecuta acciones ni mantiene estado del navegador. */
public final class HomeSelectors {
    public static final By TITLE = By.cssSelector("[data-test='title']");
    public static final By INVENTORY = By.cssSelector("[data-test='inventory-list']");
    public static final By PRODUCT = By.cssSelector("[data-test='inventory-item']");
    public static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
    public static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");

    private HomeSelectors() {}

    public static By addProduct(String productId) {
        ProductSelectorId.validate(productId);
        return By.cssSelector("[data-test='add-to-cart-" + productId + "']");
    }

    public static By removeProduct(String productId) {
        ProductSelectorId.validate(productId);
        return By.cssSelector("[data-test='remove-" + productId + "']");
    }
}
