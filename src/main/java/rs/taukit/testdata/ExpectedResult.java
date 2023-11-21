package rs.taukit.testdata;

import rs.taukit.config.Config;
import rs.taukit.element.ElementState;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageState;
import rs.taukit.records.CheckedElement;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.*;

public class ExpectedResult {
    private final String objectType;
    private final String objectName;
    private final String objectState;
    private final String expectedValue;
    private final double checkInterval;
    private final double checkTimeout;
    private final BooleanSupplier condition;

    public ExpectedResult(Map<String, String> data) {
        this.objectType = TestDataUtils.get(data, "Объект");
        this.objectName = TestDataUtils.get(data, "Название");
        this.objectState = TestDataUtils.get(data, "Состояние");
        this.expectedValue = TestDataUtils.getOptional(data, "Значение");
        String checkInterval = Optional.ofNullable(data.get("Интервал")).orElse(Config.getProperty("element.check.interval"));
        this.checkInterval = TimeStringParser.getTime(checkInterval);
        String checkTimeout = Optional.ofNullable(data.get("Ожидание")).orElse(Config.getProperty("element.check.timeout"));
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        this.condition = () -> checkCondition(objectType.toLowerCase());
    }

    public ExpectedResult(List<String> data) {
        this.objectType = data.get(0);
        this.objectName = data.get(1);
        this.objectState = data.get(2);
        this.expectedValue = null;
        String checkInterval = data.size() > 3 ? data.get(3) : Config.getProperty("element.check.interval");
        this.checkInterval = TimeStringParser.getTime(checkInterval);
        String checkTimeout = data.size() > 4 ? data.get(4) : Config.getProperty("element.check.timeout");
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        this.condition = () -> checkCondition(objectType.toLowerCase());
    }

    private boolean checkCondition(String objectType) {
        switch (objectType) {
            case "страница", "всплывающее окно":
                PageObject page = pageStorage().getPage(objectName);
                double pageTimeout = page.getPageTimeout();
                page.setPageTimeout(checkInterval);
                boolean result = PageState.getState(objectState).apply(pageStorage().getPage(objectName));
                page.setPageTimeout(pageTimeout);
                return result;
            case "вкладка":
                return driver().setActiveTab(Integer.parseInt(objectName) - 1);
            case "файл":
                String dirName = String.format("%s/%s", Config.getProperty("downloads.dir"), testContext().getScenarioName());
                return new File(dirName, objectName).exists();
            default:
                return ElementState.getState(objectState).apply(new CheckedElement(element(objectName), objectType, expectedValue));
        }
    }

    public void waitForResult() {
        try {
            BasicUtils.driver().waitForCondition(condition, checkInterval, checkTimeout, testContext().getStepAction());
            TauLogger.report("%s '%s' %s", objectType, objectName, objectState);
        } catch (Exception e) {
            if (e instanceof AutoTestBrokenException) throw e;
            String wrongState = ElementState.getWrongState(objectState);
            TauLogger.fail(e, "Фактический результат не соответствует ожидаемому.%n" +
                            "Ожидаемый результат: %s '%s' %s%n" +
                            "Фактический результат: %s '%s' %s",
                    objectType, objectName, objectState, objectType, objectName, wrongState);
        }
    }
}
