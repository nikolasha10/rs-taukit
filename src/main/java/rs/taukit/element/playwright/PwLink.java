package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.HasText;

import java.util.LinkedHashMap;

public class PwLink extends PwElement implements HasText {
    public PwLink(String elementName, Locator locator) {
        super(elementName, locator);
    }

    public PwLink(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwLink(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwLink(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwLink(String elementName, ElementModel model) {
        super(elementName, model);
    }

    @Override
    public String getText() {
        return elementPwLocator.innerText();
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
