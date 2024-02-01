package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.ElementState;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageState;
import rs.taukit.records.CheckedElement;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.*;

/**
 * Класс, описывающий ожидаемый результат проверки объекта тестирования.
 * Предоставляет методы для получения и ожидания результата проверки.
 */
@Getter
public class ExpectedResult {
    private final String objectType;
    private final String objectName;
    private final String objectState;
    private final String expectedValue;
    private final double checkTimeout;
    private final BooleanSupplier condition;
    private Exception thrownException;
    private boolean conditionMet;

    /**
     * Конструктор, формирующий объект, описывающий ожидаемый результат проверки объекта тестирования,
     * в соответствии с параметрами, переданными в словаре data.
     *
     * @param data - словарь параметров проверки объекта тестирования.
     */
    public ExpectedResult(Map<String, String> data) {
        this.objectType = TestDataUtils.get(data, "Объект");
        this.objectName = TestDataUtils.get(data, "Название");
        this.objectState = TestDataUtils.get(data, "Состояние");
        this.expectedValue = TestDataUtils.getOptional(data, "Значение");
        String checkTimeout = Optional.ofNullable(data.get("Ожидание")).orElse(Config.getProperty("element.check.timeout"));
        if (checkTimeout.isEmpty()) checkTimeout = Config.getProperty("element.check.timeout");
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        this.condition = () -> checkCondition(objectType.toLowerCase());
    }

    /**
     * Конструктор, формирующий объект, описывающий ожидаемый результат проверки объекта тестирования,
     * в соответствии с параметрами, переданными в списке data.
     *
     * @param data - список параметров проверки объекта тестирования.
     */

    public ExpectedResult(List<String> data) {
        this.objectType = data.get(0);
        this.objectName = data.get(1);
        this.objectState = data.get(2);
        this.expectedValue = null;
        String checkTimeout = data.size() > 3 ? data.get(3) : Config.getProperty("element.check.timeout");
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        this.condition = () -> checkCondition(objectType.toLowerCase());
    }

    /**
     * Метод, проверяющий, выполняются ли заданные условия для проверяемого объекта
     *
     * @param objectType - тип объекта, определяющий алгоритм проверки
     * @return true или false
     */
    private boolean checkCondition(String objectType) {
        switch (objectType) {
            case "страница", "всплывающее окно":
                String currentPageName = page().getPageName();
                if (!currentPageName.equals(objectName)) {
                    testContext().setSavedPageName(currentPageName);
                }
                PageObject page = driver().getCurrentBrowser().getPage(objectName);
                double pageTimeout = page.getPageTimeout();
                page.setPageTimeout(checkTimeout);
                boolean result = PageState.getState(objectState).apply(driver().getCurrentBrowser().getPage(objectName));
                page.setPageTimeout(pageTimeout);
                if (!result) this.thrownException = page.getThrownException();
                return result;
            case "вкладка":
                return driver().getCurrentBrowser().setActiveTab(Integer.parseInt(objectName) - 1);
            case "файл":
                String dirName = String.format("%s/%s", Config.getProperty("downloads.dir"), testContext().getScenarioName());
                return new File(dirName, objectName).exists();
            case "параметр":
                return scenarioData().get(objectName).toString().contains(expectedValue);
            default:
                return ElementState.getState(objectState).apply(new CheckedElement(element(objectName), objectType, expectedValue));
        }
    }

    /**
     * Метод, ожидающий выполнение условий в течение заданного времени.
     * В результате выполнения определяет значения полей conditionMet и throwException
     *
     * @return Объект, описывающий ожидаемый и полученный результат проверки.
     */
    public ExpectedResult waitForResult() {
        try {
            driver().waitForCondition(condition, checkTimeout);
            this.conditionMet = true;
        } catch (Exception e) {
            this.thrownException = e;
            conditionMet = false;
        }
        return this;
    }
}
