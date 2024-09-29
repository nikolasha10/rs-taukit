package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.ElementState;
import rs.taukit.element.interfaces.IHasBlockElements;
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
    private String currentValue;
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
                    scenarioStorage().setSavedPageName(currentPageName);
                    scenarioStorage().setSavedContext(page().getCurrentContext());
                }
                PageObject page = browser().getPage(objectName);
                double pageTimeout = page.getPageTimeout();
                page.setPageTimeout(checkTimeout);
                boolean result = PageState.getState(objectState).apply(driver().getCurrentBrowser().getPage(objectName));
                page.setPageTimeout(pageTimeout);
                if (!result) {
                    this.thrownException = page.getThrownException();
                    IHasBlockElements currentContext = scenarioStorage().getSavedContext();
                    browser().getPage(scenarioStorage().getSavedPageName()).setCurrentContext(currentContext);
                }
                return result;
            case "вкладка":
                return driver().getCurrentBrowser().setActiveTab(Integer.parseInt(objectName) - 1);
            case "файл":
                String dirName = String.format("%s/%s", Config.getProperty("downloads.dir"), scenario().getName());
                return new File(dirName, objectName).exists();
            case "параметр":
                this.currentValue = Optional.ofNullable(scenarioData().getOptional(objectName)).orElse("<отсутствует>").toString();
                switch (objectState){
                    case "равен": return currentValue.equals(expectedValue);
                    case "равен без учета регистра": return currentValue.equalsIgnoreCase(expectedValue);
                    case "не равен": return !currentValue.equals(expectedValue);
                    case "содержит": return currentValue.contains(expectedValue);
                    case "не содержит": return !currentValue.contains(expectedValue);
                    case "пуст": return currentValue.isEmpty();
                    case "не пуст": return !currentValue.isEmpty();
                    default: this.thrownException = new RuntimeException(String.format(
                            "Проверка состояния '%s' для объекта '%s' не реализована", objectState, objectType));
                }
            default:
                return ElementState.getState(objectState).apply(new CheckedElement(element(objectName), expectedValue));
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

    public ExpectedResult checkResult(){
        this.conditionMet = condition.getAsBoolean();
        return this;
    }
}
