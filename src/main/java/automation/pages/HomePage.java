package automation.pages;

import automation.selectors.HomeSelectors;
import automation.selectors.CartSelectors;
import org.openqa.selenium.WebDriver;
import automation.utils.WaitUtils;

public final class HomePage {
    private final WaitUtils wait;

    public HomePage(WebDriver driver) {
        wait = new WaitUtils(driver);
    }

    public String title() {
        return wait.visible(HomeSelectors.TITLE).getText();
    }

    public boolean hasProducts() {
        return wait.visible(HomeSelectors.INVENTORY).isDisplayed() && wait.visible(HomeSelectors.PRODUCT).isDisplayed();
    }

    public void addProduct(String productId) {
        wait.clickUntilVisible(HomeSelectors.addProduct(productId),
                HomeSelectors.removeProduct(productId));
        wait.visible(HomeSelectors.CART_BADGE);
    }

    public void openCart() {
        wait.clickUntilVisible(HomeSelectors.CART_LINK,
                CartSelectors.LIST);
    }
}
