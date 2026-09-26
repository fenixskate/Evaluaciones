package actions;

import config.ConfigManager;
import org.openqa.selenium.WebDriver;
import pages.CartPage;
import pages.CheckoutPage;

public final class CheckoutActions {
    private final CartPage cart;
    private final CheckoutPage checkout;

    public CheckoutActions(WebDriver driver) {
        cart = new CartPage(driver);
        checkout = new CheckoutPage(driver);
    }

    public void start() {
        cart.startCheckout();
        checkout.waitForForm();
    }

    public void completeBuyer() {
        checkout.enterBuyer(ConfigManager.get("buyer.firstName"),
                ConfigManager.get("buyer.lastName"), ConfigManager.get("buyer.postalCode"));
    }

    public void confirm() {
        checkout.finish();
    }
}
