package runners;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class TestRunner {
    @Test void apiTests() {
        int threads = Integer.parseInt(System.getProperty("parallel.threads", "1"));
        assertTrue(threads > 0, "parallel.threads debe ser positivo");
        String runId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
        String reportDir = "output/reports/" + runId;
        Results results = Runner.path("classpath:features")
                .tags(System.getProperty("tags", "~@ignore"))
                .outputCucumberJson(true).outputJunitXml(true)
                .reportDir(reportDir).parallel(threads);
        assertTrue(results.getScenariosTotal() > 0, "El filtro no selecciono escenarios");
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
