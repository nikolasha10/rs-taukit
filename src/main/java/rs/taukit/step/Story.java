package rs.taukit.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import io.qameta.allure.Step;
import rs.taukit.config.TestData;
import rs.taukit.config.TestDataWork;
import rs.taukit.context.TestContext;
import rs.taukit.drivers.DriverManager;
import rs.taukit.page.PageStorage;
import rs.taukit.utils.TauLogger;

/**
 * Реализация шагов сценария
 */
public class Story extends Utils {
    @Step("подготовить тестовые параметры")
    public static void testParameterInit() {
        Pickle pickle = getContext().getCurrentPickle();
        TestData testData = new TestData(pickle);
        testData.getDataTables().forEach(TauLogger::attachDataTable);
        getContext().setTestData(testData);
    }

    @Step("открыть браузер '{browserName}'")
    public static void openBrowser(String browserName) {
        if (getBrowser() == null || !getBrowser().browserType().name().equalsIgnoreCase(browserName)) {
            DriverManager.getInstance().getDriver().openBrowser("chromium");
        }
        TauLogger.report("Открыт браузер '%s'", browserName);
    }

    @Step("заполнить поля формы значениями:")
    public static void fillFormFields(DataTable dataTable) {
        dataTable.asLists().forEach(dataRow -> fill(dataRow.get(0), dataRow.get(1)));
        TauLogger.attachDataTable("Значения полей формы", dataTable);
    }
    @Step("открыть страницу '{pageName}'")
    public static void openPage(String pageName) {
        PageStorage.getInstance().getPage(pageName).loadPage();
        TauLogger.report("Открыта страница '%s'", pageName);
    }

    @Step("ввести в поле '{fieldName}' значение '{value}'")
    public static void fill(String fieldName, String value) {
        getPage().fill(fieldName, value);
        TauLogger.report("В поле '%s' введен текст '%s'", fieldName, value);
    }

    @Step("нажать на элемент '{elementName}'")
    public static void click(String elementName) {
        getPage().click(elementName);
        TauLogger.report("Нажали на элемент '%s'", elementName);
    }
    @Step("нажать на кнопку '{buttonName}'")
    public static void clickButton(String buttonName) {
        getPage().click(buttonName);
        TauLogger.report("Нажали на кнопку '%s'", buttonName);
    }
    @Step("дождаться отображения страницы '{pageName}'")
    public static void waitForPageLoad(String pageName) {
        PageStorage.getInstance().getPage(pageName).waitForLoad();
        TauLogger.report("Отображается страница '%s'", pageName);
    }

    @Step("сделать скриншот")
    public static void takeScreenshot() {
        getPage().takeScreenshot();
    }

}