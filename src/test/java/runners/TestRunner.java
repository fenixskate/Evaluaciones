package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/saucedemo.feature",
        tags = "not @pendiente",
        glue = {"steps", "hooks"},
        plugin = {"pretty", "html:target/cucumber-report.html", "json:target/cucumber-report.json",
                "reports.ScenarioPdfReporter"},
        monochrome = true
)
public class TestRunner {}
