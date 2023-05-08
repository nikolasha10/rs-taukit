package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.Element;
import rs.taukit.element.interfaces.IElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
public class PlaywrightElement extends Element {
    protected final PlaywrightDriver driver;
    protected Locator element;
    public PlaywrightElement(String elementName, Locator locator) {
        super(elementName, "");
        this.driver = super.driver.castTo();
        this.element = locator;
    }
    public PlaywrightElement(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }
    public PlaywrightElement(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
        this.driver = super.driver.castTo();
        this.element = driver.getLocator(locator);
    }


    public void setValue(String value) {
        element.fill(value);
        element.blur();
        String currentValue = element.inputValue();
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public String getText() {
        return element.innerText();
    }
}
