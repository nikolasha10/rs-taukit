import io.cucumber.testng.*;
import io.qameta.allure.Step;
import listeners.SuiteListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import rs.taukit.config.Config;
import rs.taukit.storage.StorageManager;
import rs.taukit.storage.TestContext;
import rs.taukit.driver.DriverManager;
import rs.taukit.utils.AllureReport;
import rs.taukit.utils.TauLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс-конфигуратор запуска тестов
 */
@CucumberOptions(
        features = "src/test/efr/resources/scenarios"
)
@Listeners({SuiteListener.class})
public class TestRunner {
    private final TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass());

    /**
     * Возвращает двумерный массив объектов, содержащий данные для запуска сценариев,
     * отмеченных тегами, переданными в параметре запуска <i>scenario.tags</i>
     **/
    @DataProvider(parallel = true)
    public Object[][] getScenariosByTag() {
        String annotations = Config.getProperty("scenario.tags").trim();
        String[] tagList = annotations.split(",\\s*");
        List<List<Object>> scenarioList = new ArrayList<>();
        Arrays.stream(tagList).forEach(tag -> scenarioList.addAll(getGroupsWithTag(tag)));
        Object[][] scenariosToRun = new Object[scenarioList.size()][];
        for (int i = 0; i < scenarioList.size(); i++) {
            scenariosToRun[i] = scenarioList.get(i).toArray();
        }
        return scenariosToRun;
    }

    private boolean mustBeAdded(Object scenarioEntity, String tag) {
        boolean isWrapper = scenarioEntity instanceof FeatureWrapper;
        return isWrapper || ((PickleWrapper) scenarioEntity).getPickle().getTags().stream()
                .anyMatch(scenarioTag -> scenarioTag.equals(tag));
    }

    private List<List<Object>> getGroupsWithTag(String tag) {
        return Arrays.stream(runner.provideScenarios())
                .map(scenarioGroup -> getScenariosWithTag(scenarioGroup, tag))
                .filter(group -> group.size() > 1).collect(Collectors.toList());
    }

    private List<Object> getScenariosWithTag(Object[] scenarioList, String tag) {
        return Arrays.stream(scenarioList).filter(scenario -> mustBeAdded(scenario, tag)).collect(Collectors.toList());
    }

    /**
     * Запускает сценарии в соответствии с данными из двумерного массива, возвращаемого методом <i>getScenarioByTag</i>
     *
     * @param pickleWrapper
     * @param cucumberFeature
     */
    @Test(dataProvider = "getScenariosByTag")
    public void runByTag(PickleWrapper pickleWrapper, FeatureWrapper cucumberFeature) throws IOException {
        Pickle pickle = pickleWrapper.getPickle();
        String scenarioName = pickle.getName();
        String functionality = cucumberFeature.toString();
        functionality = functionality.substring(1, functionality.length() - 1);
        DriverManager.getInstance().initDriver();
        TauLogger.setLogPath(functionality, scenarioName);
        StorageManager.getInstance().initTestContext(pickleWrapper, functionality);
        TauLogger.console("TEST STARTED: [%s] %s", functionality, scenarioName);
        runner.runScenario(pickle);
    }

    @AfterMethod()
    public void afterMethod(ITestResult result) {
        TestContext testContext = StorageManager.getInstance().getTestContext();
        String scenarioName = testContext.getScenarioName();
        String functionality = testContext.getFunctionality();
        String testStatus = result.getStatus() == 1 ? "PASSED" : "FAILED";
        TauLogger.console("TEST %s: [%s] %s", testStatus, functionality, scenarioName);
        long scenarioEndTime = result.getEndMillis();
        AllureReport.editReports(scenarioName, functionality, scenarioEndTime);
        TauLogger.collectLogs();
    }
}
