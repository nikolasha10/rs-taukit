package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.playwright.PwList;
import rs.taukit.element.playwright.PwTable;
import rs.taukit.step.BasicStory;
import rs.taukit.step.allure.AllureBasicTextFieldStep;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.PdfUtils;
import rs.taukit.validator.PdfValidator;

import static rs.taukit.step.utils.BasicUtils.*;

public class CucumberBasicCustomStep {
    @И("^дождаться отображения страницы \"(.+)\"$")
    public static void waitForPageLoad(String pageName) {
        BasicStory.waitForPageLoad(pageName);
    }

    @И("^дождаться отображения всплывающего окна \"(.+)\"$")
    public static void waitForPopupWindow(String pageName) {
        BasicStory.waitForPopupWindow(pageName);
    }

    @И("^дождаться закрытия всплывающего окна \"(.+)\"$")
    public static void waitForPopupClose(String pageName) {
        BasicStory.waitForPopupClose(pageName);
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
    @И("^определить значения тестовых параметров$")
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


    @И("^сделать скриншот$")
    public void takeScreenshot() {
        BasicStory.takeScreenshot();
    }

    @И("^подождать (\\d+) (секунд|минут)(?:у|ы)?$")
    public void waitForTimeout(String timeValue, String timeUnit) {
        BasicStory.waitForTimeout(timeValue, timeUnit);
    }

    @И("^дождаться выполнения условий:$")
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
        FileUtils.saveDownloadedFileAs(eval(originalFileName), eval(fileName));
    }
    @И("^сравнить печатную форму с эталоном$")
    public void validatePrintForm() {
        BasicStory.validatePrintForm();
    }
    @И("^сформировать эталон \"(.+)\" по файлу \"(.+)\" и шаблону \"(.+)\"$")
    public void createPdfStandard(String standardName, String originFileName, String templateName, DataTable dataTable) {
        PdfUtils.createPdfStandard(eval(standardName), eval(originFileName), eval(templateName), eval(dataTable));
    }
    @И("^сравнить файл \"(.+)\" с эталоном \"(.+)\"$")
    public void comparePdfWithStandard(String testedFileName, String standardName) {
        PdfUtils.comparePdfFiles(eval(testedFileName), eval(standardName));
    }
    @И("^проверить документ \"(.+)\" на соответствие эталону$")
    public void validateDocument(String fileName, DataTable dataTable) {
        new PdfValidator(fileName).validate(eval(dataTable));
    }

}
