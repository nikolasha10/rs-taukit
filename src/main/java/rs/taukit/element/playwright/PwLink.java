package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.utils.TestDataUtils;

import java.util.LinkedHashMap;

public class PwLink extends PwElement implements IHasText {
    protected final String textLocator;

    public PwLink(String elementName, Locator locator) {
        super(elementName, locator);
        this.textLocator = null;
    }

    public PwLink(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
        this.textLocator = model.get("текст");
    }
    public PwLink(String elementName, ElementModel model) {
        super(elementName, model);
        this.textLocator = model.get("текст");
    }

    @Override
    public String getText() {
        return element.innerText();
    }
}
