package rs.taukit.element.playwright;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class TextField extends PlaywrightElement implements IHasEditableValue {
    public TextField(String elementName, String locator) {
        super(elementName, locator);
    }
    public TextField(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
    }

    @Override
    @Step("заполнить поле '{this.elementName}' значением '{value}'")
    public void setValue(String value, String controlValue) {
        driver.waitForCondition(() -> {
            super.setValue(value);
            String currentValue = element.inputValue();
            return currentValue.equals(controlValue);
        });
        TauLogger.report("Поле '%s' заполнено значением '%s'", elementName, element.inputValue());
    }

    @Override
    public String getText() {
        return element.inputValue();
    }

    @Override
    @Step("заполнить поле '{this.elementName}' значением '{value}'")
    public void setValue(String value) {
        try {
            super.setValue(value);
            String currentValue = element.inputValue();
            TauLogger.report("Поле '%s' заполнено значением '%s'", elementName, currentValue);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось заполнить поле '%s' значением '%s'", elementName, value);
        }
    }

}
