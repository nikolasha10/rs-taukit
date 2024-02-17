import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import listeners.SimpleInvokedMethodListener;
import listeners.SuiteListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import rs.taukit.config.Config;
import rs.taukit.scenario.CucumberScenario;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.storage;

/**
 * Класс-конфигуратор запуска тестов
 * Аннотация @CucumberOptions должна содержать параметр features,
 * значение которого задаёт путь к сценариям текущего проекта.
 */
@CucumberOptions(
        features = "src/test/resources/scenarios"
)
@Listeners({SuiteListener.class, SimpleInvokedMethodListener.class})
public class TestRunner {
    private final TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass());

    /**
     * DataProvider - при запуске тестов автоматически формирует данные для запуска метода runByTag.
     *
     * @return двумерный массив объектов, содержащий данные для запуска сценариев,
     * отмеченных тегами, переданными в параметре запуска <i>scenario.tags</i>
     */
    @DataProvider(parallel = true)
    public synchronized Object[][] getScenariosByTags() {
        List<CucumberScenario> scenarios = storage.getScenarioList();
        if (scenarios.isEmpty()) {
            scenarios.addAll(findScenariosByTags());
        }
        List<CucumberScenario> readyToRunScenarios = scenarios.stream()
                .filter(CucumberScenario::isReadyForStart).toList();
        Object[][] startList = readyToRunScenarios.stream()
                .map(scenario -> Arrays.asList(scenario.getPickleWrapper(), scenario.getFeatureWrapper())
                        .toArray(Object[]::new))
                .toArray(Object[][]::new);
        readyToRunScenarios.forEach(scenario -> scenario.setStarted(true));
        return startList;
    }

    /**
     * @return список объектов класса CucumberScenario,
     * описывающий сценарии с как минимум одним из тегов, перечисленных в настройке scenario.tags
     */
    private List<CucumberScenario> findScenariosByTags() {
        String annotations = Config.getProperty("scenario.tags").trim();
        String[] tagList = annotations.split(",\\s*");
        return Arrays.stream(runner.provideScenarios())
                .filter(scenarioEntity -> Arrays.stream(tagList).anyMatch(tag -> hasTag(scenarioEntity, tag)))
                .map(CucumberScenario::new)
                .collect(Collectors.toList());
    }

    private boolean hasTag(Object[] scenarioEntity, String tag) {
        return scenarioEntity.length == 2 && ((PickleWrapper) scenarioEntity[0]).getPickle().getTags().contains(tag);
    }

    /**
     * Запускает сценарии в соответствии с данными из двумерного массива, возвращаемого методом <i>getScenarioByTag</i>
     *
     * @param pickleWrapper   - объект-обертка для работы с сущностью cucumber-сценария.
     * @param cucumberFeature - объект для получения описания функционала, тестируемого сценарием.
     */
    @Test(dataProvider = "getScenariosByTags")
    public void runByTag(PickleWrapper pickleWrapper, FeatureWrapper cucumberFeature) {
        storage.putScenario(storage.getScenario(pickleWrapper, cucumberFeature)).onStart(runner);
    }

    @AfterMethod()
    public void afterMethod(ITestResult result) {
        storage.getScenario().onFinish(result.getStatus(), result.getEndMillis());
    }
}
