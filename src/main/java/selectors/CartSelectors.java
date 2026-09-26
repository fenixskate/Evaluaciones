package selectors;

import org.openqa.selenium.By;

/** Localizadores de CartPage. No ejecuta acciones ni mantiene estado del navegador. */
public final class CartSelectors {
    public static final By LIST = By.cssSelector("[data-test='cart-list']");
    public static final By TITLE = By.cssSelector("[data-test='title']");
    public static final By ITEMS = By.cssSelector("[data-test='inventory-item']");
    public static final By PRODUCT_NAME = By.cssSelector("[data-test='inventory-item-name']");
    public static final By PRODUCT_PRICE = By.cssSelector("[data-test='inventory-item-price']");
    public static final By PRODUCT_QUANTITY = By.cssSelector("[data-test='item-quantity']");
    public static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
    public static final By CHECKOUT = By.cssSelector("[data-test='checkout']");

    private CartSelectors() {}

    /** Localizador relativo al articulo: evita depender del orden de la lista. */
    public static By item(String productId) {
        ProductSelectorId.validate(productId);
        return By.xpath("//*[@data-test='remove-" + productId
                + "']/ancestor::*[@data-test='inventory-item']");
    }

    public static By removeProduct(String productId) {
        ProductSelectorId.validate(productId);
        return By.cssSelector("[data-test='remove-" + productId + "']");
    }
}
