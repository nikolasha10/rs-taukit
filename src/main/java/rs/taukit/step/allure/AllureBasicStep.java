package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * Методы класса реализуют общие шаги сценариев для всех проектов
 * Аннотация @Step позволяет вывести сообщение в Allure-отчет
 */
public class AllureBasicStep extends BasicUtils {
    @Step("определить значения тестовых параметров")
    public static void testParameterInit() {
        scenarioStorage().getScenarioData().getAttachments().forEach(TauLogger::attach);
    }

    @Step("нажать на {elementType} '{elementName}'")
    public static void clickElement(String elementType, String elementName) {
        try {
            element(elementName).click();
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на кнопку \"%s\"", elementName);
        }
        scenarioStorage().saveStepAction(() -> element(elementName).click());
        TauLogger.report("Нажали на %s \"%s\"", elementType, elementName);
    }

    @Step("снимок: {screenshotName}")
    public static void takeScreenshot(String screenshotName) {
        TauLogger.attachScreenshot(screenshotName, page().takeScreenshot());
    }

    public static void waitForTimeout(String timeValue, String timeUnit) {
        TauLogger.console("Ждём %s %s", timeValue, timeUnit);
        driver().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
    }

    @Step("дождаться выполнения условий \"{conditionsName}\":")
    public static void waitForConditions(String conditionsName, DataTable dataTable) {
        waitForConditions(dataTable);
    }

    @Step("проверить выполнение условий '{conditionsName}':")
    public static void checkConditions(String conditionsName, DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        Map<String, String> expectedHeaders = dataTable.asMaps().get(0);
        List<List<String>> unexpectedResultList = new ArrayList<>();
        List<String> resultsHeaders = new ArrayList<>(expectedHeaders.keySet().stream().toList());
        if (resultsHeaders.contains("Значение")) {
            resultsHeaders.add("Текущее значение");
        }
        unexpectedResultList.add(resultsHeaders);
        dataTable.asMaps().stream()
                .map(data -> new ExpectedResult(data).checkResult())
                .filter(result -> !result.isConditionMet())
                .forEach(result -> {
                            List<String> unexpected = new ArrayList<>();
                            unexpected.add(result.getObjectType());
                            unexpected.add(result.getObjectName());
                            unexpected.add(result.getObjectState());
                            if (resultsHeaders.contains("Значение")) {
                                unexpected.add(result.getExpectedValue());
                                unexpected.add(result.getCurrentValue());
                            }
                            unexpectedResultList.add(unexpected);
                        }
                );
        if (unexpectedResultList.size() > 1) {
            Runnable runnable = () -> {
                DataTable unexpectedResultsTable = DataTable.create(unexpectedResultList, TextUtils.tableConverter);
                TauLogger.console("Результаты неуспешных проверок", unexpectedResultsTable);
                TauLogger.attachDataTable("Результаты неуспешных проверок", unexpectedResultsTable);
            };

            TauLogger.fail(runnable, "Результаты проверки условий '%s' не соответствуют ожидаемым", conditionsName);
        } else {
            TauLogger.report("Условия '%s' выполнены", conditionsName);
        }
        TauLogger.resetNotCritical();
    }

    @Step("дождаться выполнения условий:")
    public static void waitForConditionsNoName(DataTable dataTable) {
        waitForConditions(dataTable);
    }

    @Step("дождаться выполнения условий в течение {timeoutString}:")
    public static void waitForConditionsWithinTimeIntervalNoName(String timeoutString, DataTable dataTable) {
        waitForConditions(TimeStringParser.getTime(timeoutString), dataTable);
    }

    @Step("дождаться выполнения условий '{conditionsName}' в течение {timeoutString}:")
    public static void waitForConditionsWithinTimeIntervalNoName(String conditionsName, String timeoutString, DataTable dataTable) {
        waitForConditions(TimeStringParser.getTime(timeoutString), dataTable);
    }

    public static void waitForConditions(DataTable dataTable) {
        double elementCheckTimeout = Config.getTimeProperty("element.check.timeout");
        waitForConditions(elementCheckTimeout, dataTable);
    }

    public static void waitForConditions(double elementCheckTimeout, DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        TauLogger.console("Ожидается выполнение условий", dataTable);
        BooleanSupplier conditions = () -> dataTable.asMaps().stream()
                .map(data -> new ExpectedResult(data).waitForResult())
                .allMatch(ExpectedResult::isConditionMet);
        double elementCheckInterval = Config.getTimeProperty("element.check.interval");
        try {
            driver().waitForCondition(conditions, elementCheckInterval, elementCheckTimeout, scenarioStorage().getStepActions());
        } catch (Exception e) {
            if (e instanceof AutoTestBrokenException) throw e;
            ExpectedResult firstUnexpectedResult = dataTable.asMaps().stream()
                    .map(data -> new ExpectedResult(data).waitForResult())
                    .filter(result -> !result.isConditionMet())
                    .findFirst().orElse(null);
            if (firstUnexpectedResult != null) {
                if (firstUnexpectedResult.getThrownException() != null) {
                    e.addSuppressed(firstUnexpectedResult.getThrownException());
                }
                String objectType = firstUnexpectedResult.getObjectType();
                String objectName = firstUnexpectedResult.getObjectName();
                String objectState = firstUnexpectedResult.getObjectState();
                String currentValue = firstUnexpectedResult.getCurrentValue();
                String expectedValue = firstUnexpectedResult.getExpectedValue() == null
                        ? ""
                        : String.format(" \"%s\". Текущее значение: %s", firstUnexpectedResult.getExpectedValue(), currentValue);
                TauLogger.fail(e, "Не удалось дождаться выполнения условия: %s \"%s\" %s %s",
                        objectType, objectName, objectState, expectedValue);
            }
        }
        Map<String, String> result = dataTable.asMaps().stream().collect(Collectors.toMap(
                data -> data.get("Название"),
                data ->
                    Optional.ofNullable(new ExpectedResult(data).checkResult().getCurrentValue())
                            .orElse(new ExpectedResult(data).checkResult().getObjectState())

        ));
        TauLogger.attachMap("Условия выполнены", result);
        scenarioStorage().clearStepActions();
        scenarioStorage().stopToSaveActions();
        TauLogger.resetNotCritical();
    }

    @Step("[примечание] текущий контекст: блок '{blockName}'")
    public static void setBlockContext(String blockName) {
        String[] blocks = blockName.split(" -> ");
        Arrays.stream(blocks).forEach(block -> page().setCurrentContext(block));
        scenarioStorage().setCurrentContextName("блок \"%s\"", blockName);
        TauLogger.console("[примечание] текущий контекст: %s", scenarioStorage().getCurrentContextName());
    }

    public static void setCurrentPageContext() {
        page().setCurrentContext(null);
        scenarioStorage().saveStepAction(() -> page().setCurrentContext(null));
        scenarioStorage().setCurrentContextName("страница \"%s\"", scenarioStorage().getCurrentPageName());
        TauLogger.report("[примечание] текущий контекст: страница \"%s\"", scenarioStorage().getCurrentPageName());
    }

    @Step("[примечание] текущий контекст: блок '{blockName}'")
    public static void closeCurrentAndSetNewBlockContext(String blockName) {
        closeCurrentContext(false);
        page().setCurrentContext(blockName);
        TauLogger.console("[примечание] текущий контекст: блок элементов \"%s\"", blockName);
    }

    public static void closeCurrentContext(boolean haveToReport) {
        IElement currentBlock = page().dropCurrentContext();
        String currentContext = currentBlock == null
                ? String.format("страница \"%s\"", scenarioStorage().getCurrentPageName())
                : String.format("блок \"%s\"", currentBlock.getElementName());
        scenarioStorage().setCurrentContextName(currentContext);
        if (haveToReport) {
            TauLogger.report("* текущий контекст: %s", currentContext);
        }
    }

    @Step("дождаться отображения {elementType} '{elementName}'")
    public static void waitForElement(String elementType, String elementName) {
        try {
            page().waitForElement(elementName, true);
            scenarioStorage().saveStepAction(() -> page().waitForElement(elementName, true));
            TauLogger.report("Дождались отображения %s \"%s\"", elementType, elementName);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться отображения %s \"%s\" на странице \"%s\"",
                    elementType, elementName, scenarioStorage().getCurrentPageName());
        }
        TauLogger.resetNotCritical();
    }

    //    @Step("[примечание] результат следующей проверки некритичен для теста")
    public static void setNextCheckNotCritical() {
        scenarioData().put("NEXT_CHECK_NOT_CRITICAL", "NOT CRITICAL");
    }

    @Step("Отчёт")
    public static void createReport() {
        scenarioData().put("jenkinsPipelineUrl", Config.getProperty("jenkins.pipeline.url"));
        scenarioData().put("jenkinsAuth", Config.getProperty("jenkins.auth"));
        List<Map<String, ?>> standList = YamlParser.parse(Config.eval("${baseResourcesDir}/configs/stands.yaml"));
        String stands = standList.stream().map((item -> "'" + item.get("Название").toString() + "'")).toList().toString();
        scenarioData().put("stands", stands);
        scenarioData().put("currentStand", Config.getSTAND().getName());
        scenarioData().put("branches", Config.getProperty("branches"));
        scenarioData().put("defaultBranch", Config.getProperty("defaultBranch"));
        String template = FileUtils.readFile("src/main/resources/templates/finalReportTemplate.html");
        FileUtils.writeFile("target/temp/finalReport.html", eval(template));
        TauLogger.attachFile("Отчёт", "target/temp/finalReport.html");
    }

    @Step("сохранить '{value}', как '{parameterName}'")
    public static void saveValueAs(String value, String parameterName) {
        Runnable runnable = () -> scenarioData().put(parameterName, value);
        scenarioStorage().saveStepAction(runnable);
        runnable.run();
        TauLogger.report("Значение '%s' сохранено, как '%s'", value, parameterName);
    }

    @Step("сохранить значения в параметры:")
    public static void saveValues(DataTable dataTable) {
        List<List<String>> resultList = new ArrayList<>();
        resultList.add(Arrays.asList("Параметр", "Значение"));
        dataTable.asMaps().forEach(data -> {
            String value;
            try {
                value = eval(TestDataUtils.get(data, "Значение"));
            } catch (TestDataException e) {
                e.printStackTrace();
                value = "null";
            }
            resultList.add(Arrays.asList(eval(TestDataUtils.get(data, "Параметр")), value));
        });
        DataTable resultTable = DataTable.create(resultList, TextUtils.tableConverter);
        scenarioData().put(resultTable);
        TauLogger.attachDataTable("Значения параметров", resultTable);
        TauLogger.console("Значения сохранены в параметры", resultTable);
    }

    @Step("сгенерировать guid и сохранить его, как '{parameterName}'")
    public static void generateGuid(String parameterName) {
        Runnable runnable = () -> scenarioData().put(parameterName, UUID.randomUUID());
        scenarioStorage().saveStepAction(runnable);
        runnable.run();
        TauLogger.report("%s: %s", parameterName, scenarioData().get(parameterName));
    }

    @Step("кодировать значение '{value}' в base64 и сохранить, как '{parameterName}'")
    public static void base64Encoding(String value, String parameterName) {
        String encodeBytes = Base64.getEncoder().encodeToString(value.getBytes());
        scenarioData().put(parameterName, encodeBytes);
        TauLogger.report("%s: %s", parameterName, encodeBytes);
    }

    @Step("декодировать значение '{value}' из base64 и сохранить, как '{parameterName}'")
    public static void base64Decoding(String value, String parameterName) {
        byte[] decodeBytes = Base64.getDecoder().decode(value);
        String decoded = new String(decodeBytes, StandardCharsets.UTF_8);
        scenarioData().put(parameterName, decoded);
        TauLogger.report("%s: %s", parameterName, decoded);
    }

    @Step("декодировать значения из base64 и сохранить в параметры:")
    public static void base64Decoding(DataTable dataTable) {
        List<List<String>> results = new ArrayList<>();
        results.add(Arrays.asList("Параметр", "Значение"));
        dataTable.asMaps().forEach(row -> {
            String value = TestDataUtils.get(row, "Значение");
            String parameterName = TestDataUtils.get(row, "Параметр");
            byte[] decodeBytes = Base64.getDecoder().decode(value);
            String decoded = new String(decodeBytes, StandardCharsets.UTF_8);
            scenarioData().put(parameterName, decoded);
            results.add(Arrays.asList(parameterName, decoded));
        });
        DataTable resultTable = DataTable.create(results, TextUtils.tableConverter);
        TauLogger.attachDataTable("Результаты декодирования", resultTable);
    }

    @Step("сгенерировать строку длиной {length} символа из набора символов '{charSet}' и сохранить, как '{parameterName}'")
    public static void generateString(int length, String charSet, String parameterName) {
        Random random = new Random();
        byte[] chars = charSet.getBytes();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) chars[random.nextInt(charSet.length() - 1)]);
        }
        scenarioData().put(parameterName, builder.toString());
    }

    public static void chooseValueByConditions(DataTable dataTable, String parameterName) {
        Map<String, String> result = dataTable.asMaps().stream().filter(map -> {
            ExpectedResult expectedResult = new ExpectedResult(map);
            return expectedResult.checkResult().isConditionMet();
        }).findFirst().orElseThrow(() -> new RuntimeException("Не найден результат, соответствующий хотя бы одному условию"));
        String resultValue = TestDataUtils.get(result, "Результат");
        scenarioData().put(parameterName, resultValue);
        TauLogger.console("Параметр '%s' содержит '%s'", parameterName, scenarioData().get(parameterName));
    }

    @Step("разобрать значение параметра '{parameterName}', сохранив значения в параметрах:")
    public static void parseParameterValue(String parameterName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры поиска результатов", dataTable);
        List<List<String>> parsedValues = new ArrayList<>();
        String text = scenarioData().get(parameterName);
        Runnable runnable = getRunnableParseText(dataTable, parsedValues, text);
        scenarioStorage().saveStepAction(runnable);
        DataTable parsedValuesTable = DataTable.create(parsedValues, TextUtils.tableConverter);
        TauLogger.attachDataTable("Полученные значения", parsedValuesTable);
        TauLogger.console("Получены значения:", parsedValuesTable);
    }

    private static Runnable getRunnableParseText(DataTable dataTable, List<List<String>> parsedValues, String text) {
        Runnable runnable = () -> {
            parsedValues.clear();
            parsedValues.add(Arrays.asList("Параметр", "Значение"));
            dataTable.asMaps().forEach(data -> {
                String parameterName = TestDataUtils.get(data, "Параметр");
                String regExp = TestDataUtils.get(data, "Регулярное выражение");
                scenarioData().put(parameterName, TextUtils.findByRegExp(text, regExp));
                parsedValues.add(Arrays.asList(parameterName, scenarioData().get(parameterName)));
            });
        };
        runnable.run();
        return runnable;
    }

    @Step("преобразовать дату '{dateTimeString}' в другой формат:")
    public static void convertDateTimeFormat(String dateTimeString, String parameterName, DataTable dataTable) {
        Map<String, String> dataMap = TestDataUtils.transform(dataTable, "Параметр", "Значение");
        String initialPattern = TestDataUtils.get(dataMap, "исходный формат");
        String requiredPattern = TestDataUtils.get(dataMap, "требуемый формат");
        SimpleDateFormat initialDateFormat = new SimpleDateFormat(initialPattern, Locale.forLanguageTag("ru"));
        SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredPattern, Locale.forLanguageTag("ru"));
        try {
            Date dateTime = initialDateFormat.parse(dateTimeString);
            scenarioData().put(parameterName, requiredDateFormat.format(dateTime));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TauLogger.console("дата '%s' преобразована в '%s'", dateTimeString, scenarioData().get(parameterName));
    }
}