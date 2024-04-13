package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageStorage;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.storage.Storage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestData;
import rs.taukit.testdata.TestDataFile;

import java.util.Map;
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
        return driver().getCurrentBrowser().getCurrentPage();
    }

    public static IHasBlockElements context() {
        return page().getCurrentContext();
    }

    /**
     * Возвращает элемент страницы по его названию
     *
     * @param elementName - название элемента
     * @return экземпляр класса, имплементирующего интерфейс IElement
     */
    public static <T extends IElement> T element(String elementName) {
        return page().getElement(elementName);
    }

    public static TestData getCurrentUser() {
        return scenarioStorage().getCurrentUser();
    }

    public static String eval(String template) {
        ScenarioData scenarioData = scenarioStorage().getScenarioData();
        if (scenarioData == null) return template;
        String evalResult = scenarioData.evalObjectField(template);
        return scenarioData.evalStrings(evalResult);
    }

    public static <T> T evalObject(String template) {
        ScenarioData scenarioData = scenarioStorage().getScenarioData();
        return scenarioData.evalObject(template);
    }

    /**
     * Вычисляет значение параметров теста, переданных в таблице данных dataTable
     *
     * @param dataTable - таблица данных
     * @return таблица данных с вычисленными значениями параметров
     */
    public static DataTable eval(DataTable dataTable) {
        if (scenarioStorage().getScenarioData() == null) {
            scenarioStorage().setScenarioData(new ScenarioData(scenarioStorage().getPickle()));
        }
        ScenarioData scenarioData = scenarioStorage().getScenarioData();
        Function<String, String> eval = (template) -> {
            if (template == null) return null;
            String format = "\\$\\{(.+?)} -> (.+)";
            Matcher matcher = Pattern.compile(format).matcher(template);
            if (matcher.matches()) {
                if (scenarioData.evalObject(template) instanceof TestDataFile) {
                    return template;
                } else {
                    return scenarioData.evalObjectField(template);
                }
            } else {
                return scenarioData.evalStrings(template);
            }
        };
        return DataTable.create(dataTable.asLists().stream()
                .map(dataRow -> dataRow.stream()
                        .map(eval)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList()), TextUtils.tableConverter);
    }

    public static void checkDataHeaders(DataTable dataTable, String expectedHeaders) {
        Map<String, String> dataRow = dataTable.asMaps().get(0);
        String dataTableHeaders = String.format("| %s |", String.join(" | ", dataRow.keySet()));
        if (!expectedHeaders.equals(dataTableHeaders)) {
            String message = String.format("Заголовки таблицы данных не соответствуют ожидаемым:%nОжидались: %s%nПереданы:  %s",
                    expectedHeaders, dataTableHeaders);
            throw new DataTableFormatMismatchException(message);
        }
    }
}
