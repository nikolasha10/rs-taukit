package rs.taukit.element.playwright;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class CustomTextField extends TextField implements IHasEditableValue {
    public CustomTextField(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
        this.element = getDriver().getPage().getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(locator));
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
