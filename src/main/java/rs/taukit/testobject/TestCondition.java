package rs.taukit.testobject;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.driver;

/**
 * Класс, описывающий ожидаемый результат проверки объекта тестирования.
 * Предоставляет методы для получения и ожидания результата проверки.
 */
@Getter
public class TestCondition {
    private final String objectType;
    private final String objectName;
    private final String parentNodeName;
    private final String objectState;
    private final String expectedValue;
    private final double checkTimeout;
    private final BooleanSupplier condition;
    private Exception thrownException;
    private final String currentValue;
    private boolean conditionMet;

    /**
     * Конструктор, формирующий объект, описывающий ожидаемый результат проверки объекта тестирования,
     * в соответствии с параметрами, переданными в словаре data.
     *
     * @param data - словарь параметров проверки объекта тестирования.
     */
    public TestCondition(Map<String, String> data) {
        BooleanSupplier condition1;
        this.objectType = TestDataUtils.get(data, "Объект");
        this.objectName = TestDataUtils.get(data, "Название");
        this.parentNodeName = TestDataUtils.getOptional(data, "В блоке");
        this.objectState = TestDataUtils.get(data, "Состояние");
        this.expectedValue = TestDataUtils.getOptional(data, "Значение");
        String checkTimeout = Optional.ofNullable(data.get("Ожидание")).orElse(Config.getProperty("element.check.interval"));
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        TestObject testObject = new TestObject(TestObjectType.get(objectType).testObject.apply(objectName), expectedValue);
        TauLogger.console("objectName: %s, objectState: %s, expectedValue: %s", objectName, objectState, expectedValue);
        try {
            condition1 = () -> TestObjectState.get(objectState).check.apply(testObject);
        } catch (Exception e){
            condition1 = () -> false;
            TauLogger.fail(e, "objectName: %s, objectState: %s, expectedValue: %s", objectName, objectState, expectedValue);
        }
        this.condition = condition1;
        this.currentValue = TestObjectState.get(objectState).value.apply(testObject).toString();
    }

    /**
     * Метод, ожидающий выполнение условий в течение заданного времени.
     * В результате выполнения определяет значения полей conditionMet и throwException
     *
     * @return Объект, описывающий ожидаемый и полученный результат проверки.
     */
    public TestCondition waitForResult() {
        try {
            driver().waitForCondition(condition, checkTimeout);
            this.conditionMet = true;
        } catch (Exception e) {
            this.thrownException = e;
            conditionMet = false;
        }
        return this;
    }

    public TestCondition checkResult() {
        this.conditionMet = condition.getAsBoolean();
        return this;
    }
}
