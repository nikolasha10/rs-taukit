package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.PdfUtils;
import rs.taukit.validator.PdfValidator;

import static rs.taukit.utils.BasicUtils.*;

public class CucumberBasicStep {

    @И("^нажать на (элемент|поле|кнопку) \"(.+)\"$")
    public static void click(String elementType, String elementName) {
        AllureBasicStep.clickElement(elementType, elementName);
    }

    /**
     * Считывает тестовые данные для текущего сценария из yaml-файла с таким же названием,
     * как feature-файл, содержащий этот сценарий. Блок данных ищется по названию сценария.
     */
    @И("^определить значения тестовых параметров$")
    public void testParameterInitFromFile() {
        AllureBasicStep.testParameterInit();
    }

    @И("^открыть браузер$")
    public void openBrowser() {
        AllureBasicStep.openBrowser();
    }

    @И("^перейти по ссылке \"(.+)\"$")
    public void openUrl(String url) {
        driver().navigate(eval(url));
    }

    @И("^обновить страницу$")
    public void reloadPage() {
        AllureBasicStep.reloadPage();
    }

    @И("^закрыть текущую вкладку$")
    public void closeCurrentTab() {
        AllureBasicStep.closeCurrentTab();
    }

    @И("^сделать снимок страницы$")
    public void takeScreenshot() {
        AllureBasicStep.takeScreenshot(String.format("страница '%s'", testContext().getCurrentPageName()));
    }

    @И("^подождать (\\d+) (секунд|минут|секунду|минуту|секунды|минуты)$")
    public void waitForTimeout(String timeValue, String timeUnit) {
        AllureBasicStep.waitForTimeout(timeValue, timeUnit);
    }

    @И("^дождаться выполнения условий:$")
    public void expectedResultCheck(DataTable datatable) {
        AllureBasicStep.waitForConditions(eval(datatable));
    }

    @И("^дождаться отображения (элемента|поля|кнопки|ссылки|таблицы|блока) \"(.+)\"$")
    public void waitForElement(String elementType, String elementName) {
        AllureBasicStep.waitForElement(elementType, elementName);
    }

    @И("^в списке \"(.+)\" выбрать \"(.+)\"$")
    public void listValueClick(String listName, String listValue) {
        AllureBasicStep.listValueClick(listName, listValue);
    }

    @И("^сохранить файл \"(.+)\", как \"(.+)\"$")
    public void saveDownloadedFileAs(String originalFileName, String fileName) {
        FileUtils.saveDownloadedFileAs(eval(originalFileName), eval(fileName));
    }

    @И("^сравнить печатную форму с эталоном$")
    public void validatePrintForm() {
        AllureBasicStep.validatePrintForm();
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

    @И("^примечание: далее работаем в контексте блока \"(.+)\"$")
    public void setBlockContext(String blockName) {
        AllureBasicStep.setBlockContext(blockName);
    }

    @И("^примечание: далее работаем в контексте текущей страницы$")
    public void setCurrentPageContext() {
        AllureBasicStep.setCurrentPageContext();
    }

    @И("^примечание: выходим из текущего контекста и работаем в контексте блока \"(.+)\"$")
    public void closeCurrentAndSetNewContext(String blockName) {
        AllureBasicStep.closeCurrentAndSetNewBlockContext(blockName);
    }

    @И("^примечание: выходим из контекста блока \"(.+)\"$")
    public void closeBlockContext(String blockName) {
        AllureBasicStep.closeCurrentContext();
    }

}
