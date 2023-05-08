package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.utils.TestDataUtils;

import java.util.LinkedHashMap;

public class Link extends PlaywrightElement implements IHasText {
    protected final String textLocator;

    public Link(String elementName, Locator locator) {
        super(elementName, locator);
        this.textLocator = null;
    }

    public Link(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
        this.textLocator = TestDataUtils.get(elementData,"Текст");
    }

    @Override
    public String getText() {
        return element.innerText();
    }
}
