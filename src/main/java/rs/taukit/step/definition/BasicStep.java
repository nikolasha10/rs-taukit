package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.CommonManager;
import rs.taukit.utils.FileUtils;

import java.util.Optional;

import static rs.taukit.utils.BasicUtils.eval;

public class BasicStep extends CommonManager {

    @И("^нажать на (элемент|поле|кнопку|ссылку) \"(.+)\"$")
    public void click(String elementType, String elementName) {
        super.clickElement(elementType, elementName);
    }

    /**
     * Считывает тестовые данные для текущего сценария из yaml-файла с таким же названием,
     * как feature-файл, содержащий этот сценарий. Блок данных ищется по названию сценария.
     */
    @И("^определить значения тестовых параметров$")
    public void testParameterInitFromFile() {
        super.testParameterInit();
    }

    @И("^сделать снимок страницы$")
    public void takeScreenshot() {
        super.takeScreenshot(String.format("страница '%s'", scenarioStorage.getCurrentPageName()));
    }

    @И("^сделать снимок страницы и сохранить, как \"(.+)\"$")
    public void takeScreenshot(String screenshotName) {
        super.takeScreenshot(screenshotName);
    }

    @И("^подождать (\\d+) (секунд|минут|секунду|минуту|секунды|минуты)$")
    public void waitForTimeout(String timeValue, String timeUnit) {
        super.waitForTimeout(timeValue, timeUnit);
    }


    @И("^дождаться отображения (элемента|поля|кнопки|ссылки|таблицы|списка) \"(.+)\"$")
    public void waitForElement(String elementType, String elementName) {
        super.waitForElement(elementType, elementName);
    }

    @И("^сохранить файл \"(.+)\", как \"(.+)\"$")
    public void saveDownloadedFileAs(String originalFileName, String fileName) {
        FileUtils.saveDownloadedFileAs(eval(originalFileName), eval(fileName));
    }

    @И("^примечание: далее работаем в контексте контейнера \"(.+)\"$")
    public void setBlockContext(String containerName) {
        super.setBlockContext(eval(containerName));
    }

    @И("^примечание: далее работаем в контексте текущей страницы$")
    public void setCurrentPageContext() {
        super.setCurrentPageContext();
    }

    @И("^примечание: выходим из текущего контекста и работаем в контексте контейнера \"(.+)\"$")
    public void closeCurrentAndSetNewContext(String blockName) {
        closeCurrentAndSetNewBlockContext(blockName);
    }

    @И("^примечание: выходим из контекста контейнера \"(.+)\"$")
    public void closeBlockContext(String blockName) {
        closeCurrentContext(true);
    }

    @И("^примечание: выходим из текущего контекста$")
    public void closeCurrentContext() {
        closeCurrentContext(true);
    }

    @И("^примечание: результат следующей проверки некритичен для теста$")
    public void setNextCheckNotCritical() {
        super.setNextCheckNotCritical();
    }

    @И("^сохранить значение \"(.+)\", как \"(.+)\"$")
    public void saveValueAs(String value, String parameterName) {
        super.saveValueAs(eval(value), parameterName);
    }

    @И("^сохранить значения в параметры:$")
    public void saveValueAs(DataTable dataTable) {
        super.saveValues(dataTable);
    }

    @И("^сгенерировать guid и сохранить, как \"(.+)\"$")
    public void generateGuid(String parameterName) {
        super.generateGuid(parameterName);
    }

    @И("^сгенерировать строку длиной (\\d+) символ((а|ов)?) из набора символов \"(.+)\" и сохранить, как \"(.+)\"$")
    public void generateString(int length, String ending, String charSet, String parameterName) {
        super.generateString(length, Optional.ofNullable(ending).orElse(""), charSet, parameterName);
    }

    @И("^кодировать значение \"(.+)\" в base64 и сохранить, как \"(.+)\"$")
    public void base64Encoding(String value, String parameterName) {
        super.base64Encoding(eval(value), parameterName);
    }

    @И("^декодировать значение \"(.+)\" из base64 и сохранить, как \"(.+)\"$")
    public void base64Decoding(String value, String parameterName) {
        super.base64Decoding(eval(value), parameterName);
    }

    @И("^декодировать значения из base64 и сохранить в параметры:$")
    public void base64Decoding(DataTable dataTable) {
        super.base64Decoding(eval(dataTable));
    }

    @И("^служебный шаг: выбрать значение, соответствующее условиям, и сохранить, как \"(.+)\":$")
    public void chooseValueByConditions(String parameterName, DataTable dataTable) {
        super.chooseValueByConditions(eval(dataTable), parameterName);
    }

    @И("^разобрать значение параметра \"(.+)\", сохранив значения в параметрах:$")
    public void parseResponseResult(String parameterName, DataTable dataTable) {
        super.parseParameterValue(parameterName, eval(dataTable));
    }

    @И("^преобразовать значение даты \"(.+)\" в другой формат и сохранить, как \"(.+)\":$")
    public void convertDateTimeFormat(String dateTimeString, String parameterName, DataTable dataTable) {
        super.convertDateTimeFormat(eval(dateTimeString), parameterName, eval(dataTable));
    }

    @И("^вычислить дату по формуле \"(.+)\" и сохранить в формате \"(.+)\", как \"(.*)\"$")
    public void evalDateTime(String formula, String format, String parameterName) {
        super.evalDateTime(formula, format, parameterName);
    }

    @И("^увеличить значение параметра \"(.+)\" на \"(\\d+)\"$")
    public void increaseParameterValue(String parameterName, String increment) {
        super.increaseParameterValue(parameterName, increment);
    }

    @И("^создать коллекцию значений \"(.+)\":$")
    public void createCollection(String parameterName, DataTable dataTable) {
        super.createCollection(parameterName, eval(dataTable));
    }

}
