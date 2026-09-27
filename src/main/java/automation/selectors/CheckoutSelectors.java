package automation.selectors;

import org.openqa.selenium.By;

/** Localizadores de CheckoutPage. No ejecuta acciones ni mantiene estado del navegador. */
public final class CheckoutSelectors {
    public static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    public static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
    public static final By POSTAL_CODE = By.cssSelector("[data-test='postalCode']");
    public static final By CONTINUE = By.cssSelector("[data-test='continue']");
    public static final By TITLE = By.cssSelector("[data-test='title']");
    public static final By ITEMS = By.cssSelector("[data-test='inventory-item']");
    public static final By PRODUCT_NAME = By.cssSelector("[data-test='inventory-item-name']");
    public static final By PRODUCT_PRICE = By.cssSelector("[data-test='inventory-item-price']");
    public static final By PRODUCT_QUANTITY = By.cssSelector("[data-test='item-quantity']");
    public static final By SUBTOTAL = By.cssSelector("[data-test='subtotal-label']");
    public static final By TAX = By.cssSelector("[data-test='tax-label']");
    public static final By TOTAL = By.cssSelector("[data-test='total-label']");
    public static final By FINISH = By.cssSelector("[data-test='finish']");
    public static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");
    public static final By COMPLETE_TEXT = By.cssSelector("[data-test='complete-text']");

    private CheckoutSelectors() {}
}
