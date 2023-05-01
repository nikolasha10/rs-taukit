package rs.taukit.element.playwright;

import io.qameta.allure.Step;
import rs.taukit.driver.DriverManager;
import rs.taukit.element.IHasEditableValue;
import rs.taukit.utils.TauLogger;

public class TextField extends SimpleElement implements IHasEditableValue {
    public TextField(String elementName, String locator) {
        super(elementName, locator);
    }

    @Override
    @Step("заполнить поле '{this.elementName}' значением '{value}'")
    public String setValue(String value) {
        element.fill(value);
        element.blur();
        String currentValue = element.inputValue();
        TauLogger.report("Поле '%s' заполнено значением '%s'", elementName, currentValue);
        return currentValue;
    }

    @Override
    @Step("заполнить поле '{this.elementName}' значением '{value}'")
    public void setValue(String value, String controlValue) {
        DriverManager.getInstance().getDriver().waitForCondition(() -> {
            element.fill(value);
            element.blur();
            String currentValue = element.inputValue();
            return currentValue.equals(controlValue);
        });
        TauLogger.report("Поле '%s' заполнено значением '%s'", elementName, element.inputValue());
    }

    @Override
    public String getValue() {
        return element.inputValue();
    }

}
