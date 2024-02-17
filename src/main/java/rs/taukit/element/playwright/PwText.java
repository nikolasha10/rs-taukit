package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasText;

import java.util.LinkedHashMap;

public class PwText extends PwElement implements IHasText {
    public PwText(String elementName, Locator locator) {
        super(elementName, locator);
    }

    public PwText(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwText(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwText(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwText(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    @Override
    public String getText() {
        return elementPwLocator.innerText();
    }
}
