package automation.actions;

import org.openqa.selenium.WebDriver;
import automation.pages.CartPage;
import automation.pages.CheckoutPage;

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

    public void completeBuyer(String firstName, String lastName, String postalCode) {
        checkout.enterBuyer(firstName, lastName, postalCode);
    }

    public void confirm() {
        checkout.finish();
    }
}
