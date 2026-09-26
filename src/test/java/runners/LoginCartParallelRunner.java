package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/saucedemo.feature",
        tags = "@login or @carrito", glue = {"steps", "hooks"},
        plugin = {"pretty", "html:target/cucumber-login-cart.html",
                "json:target/cucumber-login-cart.json", "reports.ScenarioPdfReporter"}, monochrome = true)
public class LoginCartParallelRunner {}
