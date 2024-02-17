package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasText;

import java.util.LinkedHashMap;

public class PwButton extends PwElement implements IHasText {

    public PwButton(String elementName, Locator locator) {
        super(elementName, locator);
    }

    public PwButton(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwButton(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwButton(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwButton(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

}
