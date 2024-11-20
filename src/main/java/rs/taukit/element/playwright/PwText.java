package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.HasText;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.AutoTestBrokenException;

import java.util.LinkedHashMap;

public class PwText extends PwElement implements HasText {
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
        try {
            return elementPwLocator.innerText();
        } catch (Exception e) {
            throw new AutoTestBrokenException("Не удалось получить значение элемента '%s'", elementName);
        }
    }

    @Override
    public boolean hasText(String value) {
        return getText().equals(value);
    }

    @Override
    public Boolean containsText(String expectedText) {
        return getText().contains(expectedText);
    }


}
