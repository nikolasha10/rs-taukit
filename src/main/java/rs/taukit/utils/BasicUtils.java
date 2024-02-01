package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageStorage;
import rs.taukit.storage.StorageManager;
import rs.taukit.storage.TestContext;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestData;
import rs.taukit.testdata.TestDataFile;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BasicUtils {
    public static DriverManager driverManager() {
        return DriverManager.getInstance();
    }

    public static IWebDriver driver() {
        return driverManager().getDriver();
    }

    public static IWebBrowser browser() {
        return driver().getCurrentBrowser();
    }

    public static StorageManager storageManager() {
        return StorageManager.getInstance();
    }

    public static TestContext testContext() {
        return storageManager().getTestContext();
    }

    public static ScenarioData scenarioData() {
        return testContext().getScenarioData();
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
    public static IElement element(String elementName) {
        return Optional.ofNullable(page().getElement(elementName))
                .orElseThrow(() -> new ElementNotDescribedException(elementName, testContext().getCurrentPageName()));
    }

    public static TestData getCurrentUser() {
        return testContext().getCurrentUser();
    }

    public static String eval(String template) {
        ScenarioData scenarioData = testContext().getScenarioData();
        if (scenarioData == null) return template;
        String evalResult = scenarioData.evalObjectField(template);
        return scenarioData.evalStrings(evalResult);
    }

    public static <T> T evalObject(String template) {
        ScenarioData scenarioData = testContext().getScenarioData();
        return scenarioData.evalObject(template);
    }

    /**
     * Вычисляет значение параметров теста, переданных в таблице данных dataTable
     *
     * @param dataTable - таблица данных
     * @return таблица данных с вычисленными значениями параметров
     */
    public static DataTable eval(DataTable dataTable) {
        if (testContext().getScenarioData() == null) {
            testContext().setScenarioData(new ScenarioData(testContext().getPickle()));
        }
        ScenarioData scenarioData = testContext().getScenarioData();
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
