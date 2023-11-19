package step.allure;

import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import rs.taukit.exceptions.ScenarioNotFoundException;

import java.util.Arrays;

@CucumberOptions(
        features = "src/test/efr/resources/scenarioFragments",
        glue = {"rs.taukit.step", "step"}
)
public class SubScenario {
    private final TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass());

    public static SubScenario getInstance() {
        return Holder.INSTANCE;
    }

    public Pickle getScenarioByName(String scenarioName) {
        Object[][] scenarios = runner.provideScenarios();
        Object[] scenario = Arrays.stream(scenarios)
                .filter(item -> ((PickleWrapper) item[0]).getPickle().getName().equalsIgnoreCase(scenarioName))
                .findFirst().orElseThrow(() -> new ScenarioNotFoundException(scenarioName));
        return ((PickleWrapper) scenario[0]).getPickle();
    }

    public void runScenario(String scenarioName) {
        runner.runScenario(getScenarioByName(scenarioName));
    }

    private static class Holder {
        public static final SubScenario INSTANCE = new SubScenario();
    }
}
