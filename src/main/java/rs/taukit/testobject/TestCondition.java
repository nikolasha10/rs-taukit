package rs.taukit.testobject;

import lombok.Getter;
import rs.taukit.config.Config;
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
    private final String currentValue;
    private Exception thrownException;
    private boolean conditionMet;

    /**
     * Конструктор, формирующий объект, описывающий ожидаемый результат проверки объекта тестирования,
     * в соответствии с параметрами, переданными в словаре data.
     *
     * @param data - словарь параметров проверки объекта тестирования.
     */
    public TestCondition(Map<String, String> data) {
        this.objectType = TestDataUtils.get(data, "Объект");
        this.objectName = TestDataUtils.get(data, "Название");
        this.parentNodeName = TestDataUtils.getOptional(data, "В блоке");
        this.objectState = TestDataUtils.get(data, "Состояние");
        this.expectedValue = Optional.ofNullable(TestDataUtils.getOptional(data, "Значение")).orElse("");
        String checkTimeout = Optional.ofNullable(data.get("Ожидание")).orElse(Config.getProperty("element.check.interval"));
        this.checkTimeout = TimeStringParser.getTime(checkTimeout);
        Object object = TestObjectType.get(objectType).testObject.apply(objectName);
        TestObject testObject = new TestObject(object, expectedValue);
        this.condition = () -> TestObjectState.get(objectState).check.apply(testObject);
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
