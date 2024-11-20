package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.scenario.ScenarioManager;
import rs.taukit.testdata.TestDataDateTime;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static rs.taukit.utils.BasicUtils.eval;

/**
 * Методы класса реализуют общие шаги сценариев для всех проектов
 * Аннотация @Step позволяет вывести сообщение в Allure-отчет
 */
public class CommonManager extends StepManager {

    @Step("определить значения тестовых параметров")
    public void testParameterInit() {
        scenarioStorage.getScenarioData().getAttachments().forEach(TauLogger::attach);
    }

    @Step("нажать на {elementType} '{elementName}'")
    public void clickElement(String elementType, String elementName) {
        run(() -> getPage().getElement(elementName).click(),
                "Не удалось нажать на %s '%s'", elementType, elementName);
        TauLogger.report("Нажали на %s '%s'", elementType, elementName);
    }

    @Step("снимок: {screenshotName}")
    public void takeScreenshot(String screenshotName) {
        TauLogger.attachScreenshot(screenshotName, getPage().takeScreenshot());
    }

    public void waitForTimeout(String timeValue, String timeUnit) {
        TauLogger.console("Ждём %s %s", timeValue, timeUnit);
        getDriver().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
    }

    @Step("[примечание] текущий контекст: контейнер '{containerName}'")
    public void setBlockContext(String containerName) {
        String[] blocks = containerName.split(" -> ");
        Arrays.stream(blocks).forEach(block -> getPage().setCurrentContext(block));
        scenarioStorage.setCurrentContextName("контейнер '%s'", containerName);
        TauLogger.console("[примечание] текущий контекст: %s", scenarioStorage.getCurrentContextName());
    }

    public void setCurrentPageContext() {
        getPage().setCurrentContext(null);
        scenarioStorage.saveStepAction(() -> getPage().setCurrentContext(null));
        scenarioStorage.setCurrentContextName("страница '%s'", scenarioStorage.getCurrentPageName());
        TauLogger.report("[примечание] текущий контекст: страница '%s'", scenarioStorage.getCurrentPageName());
    }

    @Step("[примечание] текущий контекст: контейнер '{containerName}'")
    public void closeCurrentAndSetNewBlockContext(String containerName) {
        closeCurrentContext(false);
        getPage().setCurrentContext(containerName);
        TauLogger.console("[примечание] текущий контекст: контейнер '%s'", containerName);
    }

    public void closeCurrentContext(boolean haveToReport) {
        IElement currentBlock = getPage().dropCurrentContext();
        String currentContext = currentBlock == null
                ? String.format("страница '%s'", scenarioStorage.getCurrentPageName())
                : String.format("блок '%s'", currentBlock.getElementName());
        scenarioStorage.setCurrentContextName(currentContext);
        if (haveToReport) {
            TauLogger.report("* текущий контекст: %s", currentContext);
        }
    }

    @Step("дождаться отображения {elementType} '{elementName}'")
    public void waitForElement(String elementType, String elementName) {
        try {
            getPage().waitForElement(elementName, true);
            scenarioStorage.saveStepAction(() -> getPage().waitForElement(elementName, true));
            TauLogger.report("Дождались отображения %s '%s'", elementType, elementName);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться отображения %s '%s' на странице '%s'",
                    elementType, elementName, scenarioStorage.getCurrentPageName());
        }
        TauLogger.resetNotCritical();
    }

    //    @Step("[примечание] результат следующей проверки некритичен для теста")
    public void setNextCheckNotCritical() {
        scenarioData.put("NEXT_CHECK_NOT_CRITICAL", "NOT CRITICAL");
    }

    @Step("Отчёт")
    public void createReport() {
        scenarioData.put("scenarioData", ScenarioManager.getInstance().generateFinalReport());
        scenarioData.put("jenkinsPipelineUrl", Config.getProperty("jenkins.pipeline.url"));
        scenarioData.put("jenkinsAuth", Config.getProperty("jenkins.auth"));
        List<Map<String, ?>> standList = YamlParser.parse(Config.eval("${baseResourcesDir}/configs/stands.yaml"));
        String stands = standList.stream().map((item -> "'" + item.get("Название").toString() + "'")).toList().toString();
        scenarioData.put("stands", stands);
        scenarioData.put("currentStand", Config.getSTAND().getName());
        scenarioData.put("branches", Config.getProperty("branches"));
        scenarioData.put("defaultBranch", Config.getProperty("defaultBranch"));
        String template = FileUtils.readFile("src/main/resources/templates/finalReportTemplate.html");
        FileUtils.writeFile("target/temp/finalReport.html", eval(template));
        TauLogger.attachFile("Отчёт", "target/temp/finalReport.html");
    }

    @Step("сохранить '{value}', как '{parameterName}'")
    public void saveValueAs(String value, String parameterName) {
        Runnable runnable = () -> scenarioData.put(parameterName, value);
        scenarioStorage.saveStepAction(runnable);
        runnable.run();
        TauLogger.report("Значение '%s' сохранено, как '%s'", value, parameterName);
    }

    @Step("сохранить значения в параметры:")
    public void saveValues(DataTable dataTable) {
        List<List<String>> resultList = new ArrayList<>();
        resultList.add(Arrays.asList("Параметр", "Значение"));
        dataTable.asMaps().forEach(data -> {
            String value;
            try {
                value = eval(TestDataUtils.get(data, "Значение"));
            } catch (TestDataException e) {
                TauLogger.console(e);
                value = "null";
            }
            resultList.add(Arrays.asList(eval(TestDataUtils.get(data, "Параметр")), value));
        });
        DataTable resultTable = DataTable.create(resultList, TextUtils.tableConverter);
        scenarioData.put(resultTable);
        TauLogger.attachDataTable("Значения параметров", resultTable);
        TauLogger.console("Значения сохранены в параметры", resultTable);
    }

    @Step("сгенерировать guid и сохранить его, как '{parameterName}'")
    public void generateGuid(String parameterName) {
        Runnable runnable = () -> scenarioData.put(parameterName, UUID.randomUUID());
        scenarioStorage.saveStepAction(runnable);
        runnable.run();
        TauLogger.report("%s: %s", parameterName, scenarioData.get(parameterName));
    }

    @Step("кодировать значение '{value}' в base64 и сохранить, как '{parameterName}'")
    public void base64Encoding(String value, String parameterName) {
        String encodeBytes = Base64.getEncoder().encodeToString(value.getBytes());
        scenarioData.put(parameterName, encodeBytes);
        TauLogger.report("%s: %s", parameterName, encodeBytes);
    }

    @Step("декодировать значение '{value}' из base64 и сохранить, как '{parameterName}'")
    public void base64Decoding(String value, String parameterName) {
        byte[] decodeBytes = Base64.getDecoder().decode(value);
        String decoded = new String(decodeBytes, StandardCharsets.UTF_8);
        scenarioData.put(parameterName, decoded);
        TauLogger.report("%s: %s", parameterName, decoded);
    }

    @Step("декодировать значения из base64 и сохранить в параметры:")
    public void base64Decoding(DataTable dataTable) {
        List<List<String>> results = new ArrayList<>();
        results.add(Arrays.asList("Параметр", "Значение"));
        dataTable.asMaps().forEach(row -> {
            String value = TestDataUtils.get(row, "Значение");
            String parameterName = TestDataUtils.get(row, "Параметр");
            byte[] decodeBytes = Base64.getDecoder().decode(value);
            String decoded = new String(decodeBytes, StandardCharsets.UTF_8);
            scenarioData.put(parameterName, decoded);
            results.add(Arrays.asList(parameterName, decoded));
        });
        DataTable resultTable = DataTable.create(results, TextUtils.tableConverter);
        TauLogger.attachDataTable("Результаты декодирования", resultTable);
    }

    @Step("сгенерировать строку длиной {length} символа из набора символов '{charSet}' и сохранить, как '{parameterName}'")
    public void generateString(int length, String charSet, String parameterName) {
        Random random = new Random();
        byte[] chars = charSet.getBytes();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) chars[random.nextInt(charSet.length() - 1)]);
        }
        scenarioData.put(parameterName, builder.toString());
    }

    public void chooseValueByConditions(DataTable dataTable, String parameterName) {
        Map<String, String> result = dataTable.asMaps().stream()
                .filter(map -> new TestCondition(map).checkResult().isConditionMet())
                .findFirst().orElseThrow(AutoTestBrokenException.get("""
                        Не найден результат, соответствующий хотя бы одному условию"""));
        String resultValue = TestDataUtils.get(result, "Результат");
        scenarioData.put(parameterName, resultValue);
        TauLogger.console("Параметр '%s' содержит '%s'", parameterName, scenarioData.get(parameterName));
    }

    @Step("разобрать значение параметра '{parameterName}', сохранив значения в параметрах:")
    public void parseParameterValue(String parameterName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры поиска результатов", dataTable);
        List<List<String>> parsedValues = new ArrayList<>();
        String text = scenarioData.get(parameterName);
        Runnable runnable = () -> {
            parsedValues.clear();
            parsedValues.add(Arrays.asList("Параметр", "Значение"));
            dataTable.asMaps().forEach(data -> {
                String saveAs = TestDataUtils.get(data, "Параметр");
                String regExp = TestDataUtils.get(data, "Регулярное выражение");
                scenarioData.put(saveAs, TextUtils.findByRegExp(text, regExp));
                parsedValues.add(Arrays.asList(saveAs, scenarioData.get(saveAs)));
            });
        };
        run(runnable, "Не удалось разобрать значение параметра '%s'", parameterName);
        DataTable parsedValuesTable = DataTable.create(parsedValues, TextUtils.tableConverter);
        TauLogger.attachDataTable("Полученные значения", parsedValuesTable);
        TauLogger.console("Получены значения:", parsedValuesTable);
    }

    @Step("преобразовать дату '{dateTimeString}' в другой формат:")
    public void convertDateTimeFormat(String dateTimeString, String parameterName, DataTable dataTable) {
        Map<String, String> dataMap = TestDataUtils.transform(dataTable, "Параметр", "Значение");
        String initialPattern = TestDataUtils.get(dataMap, "исходный формат");
        String requiredPattern = TestDataUtils.get(dataMap, "требуемый формат");
        SimpleDateFormat initialDateFormat = new SimpleDateFormat(initialPattern, Locale.forLanguageTag("ru"));
        SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredPattern, Locale.forLanguageTag("ru"));
        try {
            Date dateTime = initialDateFormat.parse(dateTimeString);
            scenarioData.put(parameterName, requiredDateFormat.format(dateTime));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TauLogger.console("дата '%s' преобразована в '%s'", dateTimeString, scenarioData.get(parameterName));
    }

    @Step("вычислить дату по формуле '{formula}' и сохранить в формате '{format}', как '{parameterName}'")
    public void evalDateTime(String formula, String format, String parameterName) {
        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("тип", "дата");
        data.put("формат", format);
        data.put("формула", formula);
        TestDataDateTime testDataDateTime = new TestDataDateTime(parameterName, data);
        scenarioData.put(parameterName, testDataDateTime);
        TauLogger.report("Значение '%s' сохранено, как '%s'", testDataDateTime.getStringValue(), parameterName);
    }
}