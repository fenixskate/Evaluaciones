package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/saucedemo.feature",
        tags = "@checkout", glue = {"steps", "hooks"},
        plugin = {"pretty", "html:target/cucumber-checkout.html", "json:target/cucumber-checkout.json"}, monochrome = true)
public class CheckoutParallelTest {}
