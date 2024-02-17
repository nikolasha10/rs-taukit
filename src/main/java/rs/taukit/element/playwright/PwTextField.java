package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

import static rs.taukit.utils.BasicUtils.page;

public class PwTextField extends PwElement implements IHasEditableValue {
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
        driver.waitForCondition(() -> {
            super.setValue(value);
            page().waitForTimeout(1000);
            String currentValue = elementPwLocator.inputValue();
            return currentValue.equals(controlValue);
        });
    }

    @Override
    public String getText() {
        return elementPwLocator.inputValue();
    }


}
