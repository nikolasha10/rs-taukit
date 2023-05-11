package rs.taukit.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.playwright.PwList;
import rs.taukit.element.playwright.PwTable;

import static rs.taukit.step.utils.BasicUtils.*;

public class BasicScenario {
    @И("^дождаться отображения страницы \"(.+)\"$")
    public static void waitForPageLoad(String pageName) {
        BasicStory.waitForPageLoad(pageName);
    }

    @И("^дождаться отображения всплывающего окна \"(.+)\"$")
    public static void waitForPopupWindow(String pageName) {
        BasicStory.waitForPopupWindow(pageName);
    }

    @И("^ввести в поле \"(.+)\" значение \"(.+)\"$")
    public static void fill(String fieldName, String value) {
        BasicStory.setValue(fieldName, eval(value));
    }

    @И("^нажать на элемент \"(.+)\"$")
    public static void click(String elementName) {
        BasicStory.clickElement(elementName);
    }

    @И("^нажать на кнопку \"(.+)\"$")
    public static void clickButton(String buttonName) {
        BasicStory.clickButton(buttonName);
    }

    /**
     * Считывает тестовые данные для текущего сценария из yaml-файла с таким же названием,
     * как feature-файл, содержащий этот сценарий. Блок данных ищется по названию сценария.
     */
    @Когда("^подготовить тестовые параметры$")
    public void testParameterInitFromFile() {
        BasicStory.testParameterInit();
    }

    @И("^открыть браузер$")
    public void openBrowser() {
        BasicStory.openBrowser();
    }

    @И("^перейти по ссылке \"(.+)\"$")
    public void openUrl(String url) {
        driver().navigate(eval(url));
    }
    @И("^открыть страницу \"(.+)\"$")
    public void openPage(String pageName) {
        BasicStory.openPage(pageName);
    }

    @И("^заполнить поля формы значениями:$")
    public void fillFormFields(DataTable dataTable) {
        BasicStory.fillFormFields(eval(dataTable));
    }

    @И("^в списке \"(.+)\" выбрать значение \"(.+)\"$")
    public void selectListValue(String listName, String value) {
        BasicStory.selectListValue(listName, eval(value));
    }

    @И("^сделать скриншот$")
    public void takeScreenshot() {
        BasicStory.takeScreenshot();
    }

    @И("^подождать (\\d+) (секунд|минут)(?:у|ы)?$")
    public void waitForTimeout(String timeValue, String timeUnit) {
        BasicStory.waitForTimeout(timeValue, timeUnit);
    }

    @Тогда("^дождаться выполнения условий:$")
    public void expectedResultCheck(DataTable datatable) {
        BasicStory.expectedResultCheck(eval(datatable));
    }

    @И("^в списке \"(.+)\" выбрать \"(.+)\"$")
    public void listValueClick(String listName, String listValue) {
        element(listName).castTo(PwList.class).setValue(eval(listValue));
    }

    @И("^в таблице \"(.+)\" нажать на \"(.+)\" согласно условиям$")
    public void tableValueClick(String tableName, String elementName, DataTable conditions) {
        element(tableName).castTo(PwTable.class).clickOnElementByConditions(elementName, eval(conditions));
    }

    @И("^в таблице \"(.+)\" нажать на \"(.+)\", сохранив его значение, согласно условиям$")
    public void tableValueSaveAndClick(String tableName, String elementName, DataTable conditions) {
        element(tableName).castTo(PwTable.class).saveAndClickOnElementByConditions(elementName, eval(conditions));
    }

    @И("^в таблице \"(.+)\" найти строку \\((.+)\\), соответствующую условиям:$")
    public void findTableRowByConditions(String tableName, String rowName, DataTable dataTable) {
        element(tableName).castTo(IHasTableElements.class).findTableRowByConditions(rowName, eval(dataTable));
    }

    @И("^в строке \\((.+)\\) найти и сохранить значения элементов:$")
    public void tableRowSaveElementValues(String rowName, DataTable dataTable) {
        ((IHasTableElements) scenarioData().get(rowName)).tableRowSaveElementValues(rowName, eval(dataTable));
    }

    @И("^в строке \\((.+)\\) нажать на \"(.+)\"$")
    public void tableRowElementClick(String rowName, String elementName) {
        ((IHasTableElements) scenarioData().get(rowName)).tableRowElementClick(rowName, elementName);
    }

    @И("^сохранить файл \"(.+)\", как \"(.+)\"$")
    public void saveDownloadedFileAs(String originalFileName, String fileName) {
        driver().saveDownloadedFileAs(eval(originalFileName), eval(fileName));
    }

    @И("^сравнить печатную форму с эталоном$")
    public void validatePrintForm() {
        BasicStory.validatePrintForm();
    }

    @И("^сформировать шаблон \"(.+)\" в соответствии с инструкцией \"(.+)\"$")
    public void createPdfTemplate(String templateName, String instruction) {
        BasicStory.createPdfTemplate(eval(templateName), eval(instruction));
    }
    @И("^сформировать шаблон \"(.+)\" в соответствии с таблицей$")
    public void createPdfTemplate(String templateName, DataTable dataTable) {
        BasicStory.createPdfTemplate(eval(templateName), eval(dataTable));
    }

    @И("^сформировать эталон \"(.+)\" по файлу \"(.+)\" и шаблону \"(.+)\"$")
    public void createPdfStandard(String standardName, String originFileName, String templateName, DataTable dataTable) {
        BasicStory.createPdfStandard(eval(standardName), eval(originFileName), eval(templateName), eval(dataTable));
    }

    @И("^сравнить файл \"(.+)\" с эталоном \"(.+)\"$")
    public void comparePdfWithStandard(String originFileName, String standardName) {
        BasicStory.comparePdfWithStandard(eval(originFileName), eval(standardName));
    }

    @И("^сохранить значения поля \"(.+)\", как \"(.+)\"$")
    public void saveElementValue(String elementName, String parameterName) {
        BasicStory.saveElementValue(elementName, parameterName);
    }
}
