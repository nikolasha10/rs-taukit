package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.utils.FileUtils;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.scenarioStorage;

public class CucumberBasicStep {

    @И("^нажать на (элемент|поле|кнопку) \"(.+)\"$")
    public void click(String elementType, String elementName) {
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

    @И("^сделать снимок страницы$")
    public void takeScreenshot() {
        AllureBasicStep.takeScreenshot(String.format("страница \"%s\"", scenarioStorage().getCurrentPageName()));
    }

    @И("^сделать снимок страницы и сохранить, как \"(.+)\"$")
    public void takeScreenshot(String screenshotName) {
        AllureBasicStep.takeScreenshot(screenshotName);
    }

    @И("^подождать (\\d+) (секунд|минут|секунду|минуту|секунды|минуты)$")
    public void waitForTimeout(String timeValue, String timeUnit) {
        AllureBasicStep.waitForTimeout(timeValue, timeUnit);
    }

    @И("^проверить выполнение условий \"(.+)\":$")
    public void checkConditions(String conditionsName, DataTable datatable) {
        AllureBasicStep.checkConditions(conditionsName, eval(datatable));
    }

    @И("^дождаться выполнения условий:$")
    public void waitForConditions(DataTable datatable) {
        AllureBasicStep.waitForConditionsNoName(eval(datatable));
    }

    @И("^дождаться выполнения условий в течение \"(.+)\":$")
    public void expectedResultCheckWithinTimeInterval(String timeoutString, DataTable datatable) {
        AllureBasicStep.waitForConditionsWithinTimeIntervalNoName(timeoutString, eval(datatable));
    }

    @И("^дождаться выполнения условий \"(.+)\" в течение \"(.+)\":$")
    public void expectedResultCheckWithinTimeInterval(String conditionsName, String timeoutString, DataTable datatable) {
        AllureBasicStep.waitForConditionsWithinTimeIntervalNoName(conditionsName, timeoutString, eval(datatable));
    }

    /**
     * Ожидает выполнение условий, перечисленных в таблице datatable
     *
     * @param conditionsName название блока условий, на которое ссылается служебный шаг повторения действий
     * @param datatable      таблица условий. Формат:<br>
     *                       <nobr>| Объект | Название | Состояние | Значение | Ожидание |</nobr>
     *                       <br><br>Объект:
     *                       <ul>
     *                           <li>вкладка</li>
     *                           <li>страница</li>
     *                           <li>[любой тип элемента]</li>
     *                       </ul>
     */
    @И("^дождаться выполнения условий \"([а-яА-Я\\w\\d\\s':/]+)\":$")
    public void waitForConditions(String conditionsName, DataTable datatable) {
        AllureBasicStep.waitForConditions(conditionsName, eval(datatable));
    }

    @И("^дождаться отображения (элемента|поля|кнопки|ссылки|таблицы) \"(.+)\"$")
    public void waitForElement(String elementType, String elementName) {
        AllureBasicStep.waitForElement(elementType, elementName);
    }

    @И("^сохранить файл \"(.+)\", как \"(.+)\"$")
    public void saveDownloadedFileAs(String originalFileName, String fileName) {
        FileUtils.saveDownloadedFileAs(eval(originalFileName), eval(fileName));
    }

    @И("^примечание: далее работаем в контексте блока \"(.+)\"$")
    public void setBlockContext(String blockName) {
        AllureBasicStep.setBlockContext(eval(blockName));
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
        AllureBasicStep.closeCurrentContext(true);
    }

    @И("^примечание: результат следующей проверки некритичен для теста$")
    public void setNextCheckNotCritical() {
        AllureBasicStep.setNextCheckNotCritical();
    }

    @И("^сформировать отчёт$")
    public void createReport() {
        AllureBasicStep.createReport();
    }

    @И("^сохранить значение \"(.+)\", как \"(.+)\"$")
    public void saveValueAs(String value, String parameterName) {
        AllureBasicStep.saveValueAs(eval(value), parameterName);
    }

    @И("^сохранить значения в параметры:$")
    public void saveValueAs(DataTable dataTable) {
        AllureBasicStep.saveValues(dataTable);
    }

    @И("^сгенерировать guid и сохранить, как \"(.+)\"$")
    public void generateGuid(String parameterName) {
        AllureBasicStep.generateGuid(parameterName);
    }

    @И("^сгенерировать строку длиной (\\d+) символа из набора символов \"(.+)\" и сохранить, как \"(.+)\"$")
    public void generateString(int length, String charSet, String parameterName) {
        AllureBasicStep.generateString(length, charSet, parameterName);
    }

    @И("^кодировать значение \"(.+)\" в base64 и сохранить, как \"(.+)\"$")
    public void base64Encoding(String value, String parameterName) {
        AllureBasicStep.base64Encoding(eval(value), parameterName);
    }

    @И("^декодировать значение \"(.+)\" из base64 и сохранить, как \"(.+)\"$")
    public void base64Decoding(String value, String parameterName) {
        AllureBasicStep.base64Decoding(eval(value), parameterName);
    }

    @И("^декодировать значения из base64 и сохранить в параметры:$")
    public void base64Decoding(DataTable dataTable) {
        AllureBasicStep.base64Decoding(eval(dataTable));
    }

    @И("^служебный шаг: выбрать значение, соответствующее условиям, и сохранить, как \"(.+)\":$")
    public void chooseValueByConditions(String parameterName, DataTable dataTable) {
        AllureBasicStep.chooseValueByConditions(eval(dataTable), parameterName);
    }

    @И("^разобрать значение параметра \"(.+)\", сохранив значения в параметрах:$")
    public void parseResponseResult(String parameterName, DataTable dataTable) {
        AllureBasicStep.parseParameterValue(parameterName, eval(dataTable));
    }

    @И("^преобразовать значение даты \"(.+)\" в другой формат и сохранить, как \"(.+)\":$")
    public void convertDateTimeFormat(String dateTimeString, String parameterName, DataTable dataTable) {
        AllureBasicStep.convertDateTimeFormat(eval(dateTimeString), parameterName, eval(dataTable));
    }

    @И("^вычислить дату по формуле \"(.+)\" и сохранить в формате \"(.+)\", как \"(.*)\"$")
    public void evalDateTime(String formula, String format, String parameterName) {
        AllureBasicStep.evalDateTime(formula, format, parameterName);
    }

}
