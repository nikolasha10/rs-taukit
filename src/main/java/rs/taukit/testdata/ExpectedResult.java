package rs.taukit.testdata;

import rs.taukit.config.Config;
import rs.taukit.element.ElementState;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.page.PageObject;
import rs.taukit.step.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static rs.taukit.step.utils.BasicUtils.*;

public class ExpectedResult {
    private final Map<String, String> data;
    private final PageObject pageObject;
    private final String objectType;
    private final String objectName;
    private final String objectState;
    private final double checkInterval;
    private final double checkTimeout;
    private final BooleanSupplier condition;

    public ExpectedResult(Map<String, String> data, PageObject pageObject) {
        this.data = data;
        this.pageObject = pageObject;
        this.objectType = TestDataUtils.get(data, "Объект");
        this.objectName = TestDataUtils.get(data, "Название");
        this.objectState = TestDataUtils.get(data, "Состояние");
        String checkInterval = Optional.ofNullable(data.get("Интервал")).orElse(Config.getProperty("element.check.interval"));
        this.checkInterval = TimeStringParser.getTime(checkInterval);
        String checkTimeout = Optional.ofNullable(data.get("Ожидание")).orElse(Config.getProperty("element.check.timeout"));
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        this.condition = () -> {
            if (objectType.equalsIgnoreCase("страница")) {
                return pageStorage().getPage(objectName).isPageLoaded();
            } else if (objectType.equalsIgnoreCase("вкладка")) {
                return driver().setActiveTab(Integer.parseInt(objectName)-1);
            } else {
                return ElementState.getState(objectState).apply(element(objectName));
            }
        };
    }

    public void waitForResult() {
        try {
            BasicUtils.driver().waitForCondition(condition, checkInterval, checkTimeout, testContext().getStepAction());
            TauLogger.report("%s '%s' %s", objectType, objectName, objectState);
        } catch (Exception e) {
            if (e instanceof AutoTestBrokenException) throw e;
            String wrongState = ElementState.getWrongState(objectState);
            TauLogger.fail(e,"Фактический результат не соответствует ожидаемому.%n" +
                            "Ожидаемый результат: %s '%s' %s%n" +
                            "Фактический результат: %s '%s' %s",
                    objectType, objectName, objectState, objectType, objectName, wrongState);
        }
    }
}
