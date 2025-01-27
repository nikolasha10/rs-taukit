package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageStorage;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.storage.Storage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestData;
import rs.taukit.testdata.TestDataFile;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BasicUtils {

    public static Storage storage = Storage.getInstance();

    public static CucumberScenario scenario() {
        return storage.getScenario();
    }

    public static ScenarioStorage scenarioStorage() {
        return scenario().getScenarioStorage();
    }

    public static DriverManager driverManager() {
        return scenarioStorage().getDriverManager();
    }

    public static IWebDriver driver() {
        return driverManager().getCurrentDriver();
    }

    public static IWebBrowser browser() {
        return driver().getCurrentBrowser();
    }

    public static ScenarioData scenarioData() {
        return scenarioStorage().getScenarioData();
    }

    public static PageStorage pageStorage() {
        return PageStorage.getInstance();
    }

    public static PageObject page() {
        IWebBrowser browser = driver().getCurrentBrowser();
        return browser == null ? null : browser.getCurrentPage();
    }

    public static IContainer context() {
        return Objects.requireNonNull(page()).getCurrentContext();
    }

    /**
     * Возвращает элемент страницы по его названию
     *
     * @param elementName - название элемента
     * @return экземпляр класса, имплементирующего интерфейс IElement
     */
    public static <T extends IElement> T element(String elementName) {
        return Objects.requireNonNull(page()).getElement(elementName);
    }

    public static TestData getCurrentUser() {
        return scenarioStorage().getCurrentUser();
    }

    public static String eval(String template) {
        ScenarioData scenarioData = scenarioStorage().getScenarioData();
        if (scenarioData == null) return template;
        String evalResult = storage.evalObjectField(template);
        return storage.evalString(evalResult);
    }

    public static <T> T evalObject(String template) {
        return storage.evalObject(template);
    }

    /**
     * Вычисляет значение параметров теста, переданных в таблице данных dataTable
     *
     * @param dataTable - таблица данных
     * @return таблица данных с вычисленными значениями параметров
     */
    public static DataTable eval(DataTable dataTable) {
        Function<String, String> evaluate = (template) -> {
            if (template == null) return null;
            String format = "\\$\\{(.+?)} -> (.+)";
            Matcher matcher = Pattern.compile(format).matcher(template);
            if (matcher.matches()) {
                if (storage.evalObject(template) instanceof TestDataFile) {
                    return template;
                } else {
                    return storage.evalObjectField(template);
                }
            } else {
                return storage.evalString(template);
            }
        };
        return DataTable.create(dataTable.asLists().stream()
                .map(dataRow -> dataRow.stream()
                        .map(evaluate)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList()), TextUtils.tableConverter);
    }

    public static boolean skipped() {
        Boolean skipActions = scenarioData().getOptional("SKIP_ACTIONS");
        return Optional.ofNullable(skipActions).orElse(false);
    }

    public static void run(Runnable action) {
        Runnable runnable = () -> {
            if (!skipped()) {
                action.run();
            }
        };
        runnable.run();
        scenarioStorage().saveStepAction(runnable);
    }

    public static void run(Runnable action, String format, Object... args) {
        Runnable runnable = () -> {
            if (!skipped()) {
                try {
                    action.run();
                } catch (Exception e) {
                    TauLogger.fail(e, format, args);
                }
            }
        };
        runnable.run();
        scenarioStorage().saveStepAction(runnable);
    }
}
