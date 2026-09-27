package automation.pages;

import automation.selectors.CartSelectors;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import automation.utils.WaitUtils;

public final class CartPage {
    private final WebDriver driver;
    private final WaitUtils wait;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        wait = new WaitUtils(driver);
    }

    public String title() {
        wait.visible(CartSelectors.LIST);
        return wait.visible(CartSelectors.TITLE).getText();
    }

    private WebElement item(String productId) {
        return wait.visible(CartSelectors.item(productId));
    }

    public String productName(String productId) {
        return item(productId).findElement(CartSelectors.PRODUCT_NAME).getText();
    }

    public String productPrice(String productId) {
        return item(productId).findElement(CartSelectors.PRODUCT_PRICE).getText();
    }

    public String productQuantity(String productId) {
        return item(productId).findElement(CartSelectors.PRODUCT_QUANTITY).getText();
    }

    public void removeProduct(String productId) {
        item(productId).findElement(CartSelectors.removeProduct(productId)).click();
        wait.absent(CartSelectors.ITEMS);
        wait.absent(CartSelectors.CART_BADGE);
    }

    public boolean isEmpty() {
        title();
        return driver.findElements(CartSelectors.ITEMS).isEmpty()
                && driver.findElements(CartSelectors.CART_BADGE).isEmpty();
    }

    public void startCheckout() {
        wait.clickable(CartSelectors.CHECKOUT).click();
    }
}
