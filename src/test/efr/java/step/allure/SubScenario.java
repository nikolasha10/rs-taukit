package step.allure;

import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import rs.taukit.exceptions.ScenarioNotFoundException;
import rs.taukit.utils.TauLogger;

import java.util.Arrays;
import java.util.function.Predicate;

import static rs.taukit.utils.BasicUtils.testContext;

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

    public Pickle getScenarioByName(String functionality, String scenarioName) {
        Object[][] scenarios = runner.provideScenarios();
        Object[] scenario = Arrays.stream(scenarios)
                .filter(item -> hasExpectedName(functionality, scenarioName).test(item))
                .findFirst().orElseThrow(() -> new ScenarioNotFoundException(scenarioName));
        return ((PickleWrapper) scenario[0]).getPickle();
    }

    public void runScenario(String scenarioName) {
        TauLogger.console("*** выполняется сценарий: %s", scenarioName);
        String currentPageName = testContext().getCurrentPageName();
        String currentContextName = testContext().getCurrentContextName();
        if (currentPageName != null) {
            TauLogger.report("[примечание] открыта страница: %s", testContext().getCurrentPageName());
        }
        if (currentContextName != null) {
            TauLogger.report("[примечание] текущий контекст: %s", testContext().getCurrentContextName());
        }
        new TestNGCucumberRunner(this.getClass()).runScenario(getScenarioByName(scenarioName));
        TauLogger.console("*** сценарий выполнен: %s", scenarioName);
    }

    public void runScenario(String functionality, String scenarioName) {
        TauLogger.console("*** выполняется сценарий: %s", scenarioName);
        String currentPageName = testContext().getCurrentPageName();
        String currentContextName = testContext().getCurrentContextName();
        if (currentPageName != null) {
            TauLogger.report("[примечание] открыта страница: %s", testContext().getCurrentPageName());
        }
        if (currentContextName != null) {
            TauLogger.report("[примечание] текущий контекст: %s", testContext().getCurrentContextName());
        }
        new TestNGCucumberRunner(this.getClass()).runScenario(getScenarioByName(functionality, scenarioName));
        TauLogger.console("*** сценарий выполнен: %s", scenarioName);
    }

    private Predicate<Object[]> hasExpectedName(String scenarioName) {
        return (Object[] items) -> ((PickleWrapper) items[0]).getPickle().getName().equalsIgnoreCase(scenarioName);
    }

    private Predicate<Object[]> hasExpectedName(String functionality, String scenarioName) {
        return (Object[] items) -> {
            String checkedName = items[1].toString().substring(1, items[1].toString().length() - 1);
            return checkedName.equalsIgnoreCase(functionality)
                    && ((PickleWrapper) items[0]).getPickle().getName().equalsIgnoreCase(scenarioName);
        };
    }

    private static class Holder {
        public static final SubScenario INSTANCE = new SubScenario();
    }
}
