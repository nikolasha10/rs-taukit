package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

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

    @Step("дождаться выполнения условий:")
    public static void waitForConditionsNoName(DataTable dataTable) {
        waitForConditions(dataTable);
    }

    @Step("дождаться выполнения условий в течение {timeoutString}:")
    public static void waitForConditionsWithinTimeIntervalNoName(String timeoutString, DataTable dataTable) {
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
                String expectedValue = firstUnexpectedResult.getExpectedValue() == null
                        ? ""
                        : String.format(" \"%s\"", firstUnexpectedResult.getExpectedValue());
                TauLogger.fail(e, "Не удалось дождаться выполнения условия: %s \"%s\" %s %s",
                        objectType, objectName, objectState, expectedValue);
            }
        }
        TauLogger.report("Условия выполнены");
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

    @Step("[примечание] результат следующей проверки некритичен для теста")
    public static void setNextCheckNotCritical() {
        scenarioData().put("NEXT_CHECK_NOT_CRITICAL", "NOT CRITICAL");
    }
}