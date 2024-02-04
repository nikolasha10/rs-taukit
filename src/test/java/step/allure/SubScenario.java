package step.allure;

import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.exceptions.ScenarioNotFoundException;
import rs.taukit.utils.TauLogger;

import java.util.Arrays;
import java.util.function.Predicate;

import static rs.taukit.utils.BasicUtils.*;

@CucumberOptions(
        features = "src/test/resources/scenarioFragments",
        glue = {"rs.taukit.step", "step"}
)
public class SubScenario {
    private final TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass());

    public static SubScenario getInstance() {
        return Holder.INSTANCE;
    }

    public Pickle getScenarioByName(String functionality, String scenarioName) {
        Object[][] scenarios = runner.provideScenarios();
        Object[] scenario = Arrays.stream(scenarios)
                .filter(item -> hasExpectedName(functionality, scenarioName).test(item))
                .findFirst().orElseThrow(() -> new ScenarioNotFoundException(scenarioName));
        return ((PickleWrapper) scenario[0]).getPickle();
    }

    public void runScenario(String functionality, String scenarioName) {
        TauLogger.console("*-> выполняется сценарий: %s", scenarioName);
        if (driver().isBrowserOpen() && browser().isPageOpen()) {
            TauLogger.report("[примечание] открыта страница: %s", page().getPageName());
            IHasBlockElements currentContext = page().getCurrentContext();
            if (currentContext != null) {
                TauLogger.report("[примечание] текущий контекст: %s \"%s\"",
                        currentContext.getModel().getType(), currentContext.getElementName());
            }
        }
        new TestNGCucumberRunner(this.getClass()).runScenario(getScenarioByName(functionality, scenarioName));
        TauLogger.console("<-* сценарий выполнен: %s", scenarioName);
    }

    private Predicate<Object[]> hasExpectedName(String functionality, String scenarioName) {
        return (Object[] items) -> {
            String checkedName = items[1].toString().substring(1, items[1].toString().length() - 1);
            return (functionality == null || checkedName.equalsIgnoreCase(functionality))
                    && ((PickleWrapper) items[0]).getPickle().getName().equalsIgnoreCase(scenarioName);
        };
    }

    private static class Holder {
        public static final SubScenario INSTANCE = new SubScenario();
    }
}
