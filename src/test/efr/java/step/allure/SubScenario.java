package step.allure;

import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import rs.taukit.exceptions.ScenarioNotFoundException;

import java.util.Arrays;
import java.util.function.Predicate;

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
                .filter(item -> hasExpectedName(scenarioName).test(item))
                .findFirst().orElseThrow(() -> new ScenarioNotFoundException(scenarioName));
        return ((PickleWrapper) scenario[0]).getPickle();
    }

    public void runScenario(String scenarioName) {
        new TestNGCucumberRunner(this.getClass()).runScenario(getScenarioByName(scenarioName));
    }

    private Predicate<Object[]> hasExpectedName(String scenarioName) {
        String[] chunks = scenarioName.split(" -> ");
        String scenarioRealName = chunks.length > 1 ? chunks[1] : scenarioName;
        String functionality = chunks[0];
        return (Object[] items) -> {
            boolean isEquals = ((PickleWrapper) items[0]).getPickle().getName().equalsIgnoreCase(scenarioRealName);
            if (chunks.length == 2) {
                String checkedName = items[1].toString().substring(1, items[1].toString().length() - 1);
                isEquals = isEquals && checkedName.equalsIgnoreCase(functionality);
            }
            return isEquals;
        };
    }

    private static class Holder {
        public static final SubScenario INSTANCE = new SubScenario();
    }
}
