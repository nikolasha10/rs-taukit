package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

import static rs.taukit.utils.BasicUtils.page;

public class PwTextField extends PwElement implements Editable {
    public PwTextField(String elementName, String selector) {
        super(elementName, selector);
    }

    public PwTextField(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwTextField(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwTextField(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwTextField(String elementName, IElement element) {
        super(elementName, element);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    public void setValue(String value, String controlValue) {
        String controlValueFinal = controlValue.equals("=") ? value : controlValue;
        driver.waitForCondition(() -> {
            super.setValue(value);
            page().waitForTimeout(1000);
            String currentValue = elementPwLocator.inputValue();
            if (!currentValue.equals(controlValueFinal)) {
                TauLogger.console("currentValue: %s, controlValue: %s", currentValue, controlValueFinal);
            }
            return currentValue.equals(controlValueFinal);
        });
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public String getText() {
        return elementPwLocator.inputValue();
    }

    @Override
    public boolean hasText(String expectedValue) {
        return getText().equals(expectedValue);
    }

    @Override
    public Boolean containsText(String expectedText) {
        return getText().contains(expectedText);
    }

}
