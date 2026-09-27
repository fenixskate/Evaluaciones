package automation.pages;

import org.openqa.selenium.By;
import automation.selectors.CheckoutSelectors;
import org.openqa.selenium.WebDriver;
import automation.utils.WaitUtils;

public final class CheckoutPage {
    private final WebDriver driver;
    private final WaitUtils wait;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        wait = new WaitUtils(driver);
    }

    public void waitForForm() {
        wait.visible(CheckoutSelectors.FIRST_NAME);
        wait.visible(CheckoutSelectors.LAST_NAME);
        wait.visible(CheckoutSelectors.POSTAL_CODE);
    }

    private void fill(By locator, String value) {
        wait.visible(locator).clear();
        wait.visible(locator).sendKeys(value);
    }

    public void enterBuyer(String firstName, String lastName, String postalCode) {
        fill(CheckoutSelectors.FIRST_NAME, firstName);
        fill(CheckoutSelectors.LAST_NAME, lastName);
        fill(CheckoutSelectors.POSTAL_CODE, postalCode);
        wait.clickable(CheckoutSelectors.CONTINUE).click();
        wait.visible(CheckoutSelectors.SUBTOTAL);
    }

    private String text(By locator) {
        return wait.visible(locator).getText();
    }

    public String title() {
        return text(CheckoutSelectors.TITLE);
    }

    public String productName() {
        return text(CheckoutSelectors.PRODUCT_NAME);
    }

    public String productPrice() {
        return text(CheckoutSelectors.PRODUCT_PRICE);
    }

    public String productQuantity() {
        return text(CheckoutSelectors.PRODUCT_QUANTITY);
    }

    public String subtotal() {
        return text(CheckoutSelectors.SUBTOTAL);
    }

    public String tax() {
        return text(CheckoutSelectors.TAX);
    }

    public String total() {
        return text(CheckoutSelectors.TOTAL);
    }

    public String confirmationHeader() {
        return text(CheckoutSelectors.COMPLETE_HEADER);
    }

    public String confirmationText() {
        return text(CheckoutSelectors.COMPLETE_TEXT);
    }

    public int productCount() {
        wait.visible(CheckoutSelectors.ITEMS);
        return driver.findElements(CheckoutSelectors.ITEMS).size();
    }

    public void finish() {
        wait.clickable(CheckoutSelectors.FINISH).click();
        wait.visible(CheckoutSelectors.COMPLETE_HEADER);
    }
}
