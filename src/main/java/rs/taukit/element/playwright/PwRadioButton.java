package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwRadioButton extends PwList implements IHasEditableValue {
    public PwRadioButton(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwRadioButton(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwRadioButton(String elementName, IElement element) {
        super(elementName, element);
    }

    public PwRadioButton(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    @Override
    public void setValue(String value) {
        super.setValueAction(value);
    }
}
