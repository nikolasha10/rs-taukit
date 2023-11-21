package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasText;

import java.util.LinkedHashMap;

public class PwButton extends PwElement implements IHasText {
    protected final String textLocator;

    public PwButton(String elementName, Locator locator) {
        super(elementName, locator);
        this.textLocator = getLocator();
    }

    public PwButton(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwButton(String elementName, ElementModel model) {
        super(elementName, model);
        this.textLocator = getLocator();
    }

    public PwButton(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    @Override
    public String getText() {
        return element.innerText();
    }
}
