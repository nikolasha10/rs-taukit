package rs.taukit.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.IElement;
import rs.taukit.element.IHasEditableValue;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.testdata.FormData;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.driver.DriverManager;
import rs.taukit.step.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

/**
 * Методы класса реализуют общие шаги сценариев для всех проектов
 * Аннотация @Step позволяет вывести сообщение в Allure-отчет
 */
public class BasicStory extends BasicUtils {
    @Step("подготовить тестовые параметры")
    public static void testParameterInit() {
        Pickle pickle = testContext().getPickle();
        ScenarioData scenarioData = new ScenarioData(pickle);
        scenarioData.getAttachments().forEach(TauLogger::attach);
        testContext().setScenarioData(scenarioData);
    }

    @Step("открыть браузер")
    public static void openBrowser() {
        if (!driver().isBrowserOpen()) {
            DriverManager.getInstance().getDriver().openBrowser();
        }
        TauLogger.report("Открыт браузер '%s'", driver().getBrowserType());
    }

    @Step("заполнить поля формы значениями:")
    public static void fillFormFields(DataTable dataTable) {
        FormData formData = new FormData(dataTable,page());
        formData.fillForm();
        TauLogger.attachDataTable("Значения полей формы", dataTable);
    }

    @Step("открыть страницу '{pageName}'")
    public static void openPage(String pageName) {
        pageStorage().getPage(pageName).loadPage();
        TauLogger.report("Открыта страница '%s'", pageName);
    }

    public static void setValue(String fieldName, String value) {
        Class<IElement> elementType = ElementType.getType("поле").getElementClass();
        page().replaceElement(fieldName,elementType).castTo(IHasEditableValue.class).setValue(value);
    }

    @Step("нажать на элемент '{elementName}'")
    public static void clickElement(String elementName) {
        element(elementName).click();
        TauLogger.report("Нажали на элемент '%s'", elementName);
    }

    @Step("нажать на кнопку '{buttonName}'")
    public static void clickButton(String buttonName) {
        page().waitForElement(buttonName, true);
        testContext().setStepActionPageName(testContext().getCurrentPageName());
        element(buttonName).click();
        testContext().setStepAction(() -> {
            element(buttonName).click();
            return true;
        });
        TauLogger.report("Нажали на кнопку '%s'", buttonName);
    }

    @Step("дождаться отображения страницы '{pageName}'")
    public static void waitForPageLoad(String pageName) {
        pageStorage().getPage(pageName).waitForLoad();
        TauLogger.report("Отображается страница '%s'", pageName);
    }

    @Step("дождаться отображения всплывающего окна '{pageName}'")
    public static void waitForPopupWindow(String pageName) {
        pageStorage().getPopup(pageName).waitForLoad();
        TauLogger.report("Отображается всплывающее окно '%s'", pageName);
    }

    @Step("сделать скриншот")
    public static void takeScreenshot() {
        TauLogger.attachScreenshot(page().takeScreenshot());
    }

    @Step("в списке '{listName}' выбрать значение '{value}'")
    public static void selectListValue(String listName, String value) {
        element(listName).castTo(IHasEditableValue.class).setValue(value);
        TauLogger.report("В списке '%s' выбран элемент '%s'", listName, value);
    }

    public static void waitForTimeout(String timeValue, String timeUnit) {
        page().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
    }

    @Step("ожидаемый результат:")
    public static void expectedResultCheck(DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        dataTable.asMaps().forEach(data -> new ExpectedResult(data, page()).waitForResult());
//        String expectedHeaders = "| Объект | Название | Состояние | Интервал | Ожидание |";
//        checkDataHeaders(dataTable, expectedHeaders);
//        BooleanSupplier condition = () -> {
//            return dataTable.asMaps().stream().allMatch(dataRow -> {
//                String objectType = dataRow.get("Объект");
//                String objectName = dataRow.get("Название");
//                String objectState = dataRow.get("Состояние");
//                String checkInterval = dataRow.get("Интервал");
//                String timeout = dataRow.get("Ожидание");
//                if (objectType.equalsIgnoreCase("кнопка")){
//                    try {
//                        return element(objectName).isVisible() == objectState.equals("Отображается");
//                    } catch (ElementNotDescribedException e){
//                        return !objectState.equals("Отображается");
//                    }
//                } else if (objectType.equalsIgnoreCase("страница")) {
//                    return pageStorage().getPage(objectName).isPageLoaded();
//                }
//                return false;
//            });
//        };
//        BooleanSupplier previousStep = testContext().getStepAction();
//        driver().waitForCondition(condition, previousStep);
    }


}