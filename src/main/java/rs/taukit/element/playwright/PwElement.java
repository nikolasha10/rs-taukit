package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.Element;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class PwElement extends Element {
    protected final PlaywrightDriver driver;
    protected Locator element;

    public PwElement(String elementName, Locator locator) {
        super(elementName, "");
        this.driver = super.driver.castTo();
        this.element = locator;
    }

    public PwElement(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }

    public PwElement(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
        this.driver = super.driver.castTo();
        this.element = driver.getLocator(locator);
    }

    public PwElement(String elementName, ElementModel model) {
        super(elementName, model);
        this.driver = super.driver.castTo();
        this.element = driver.getLocator(locator);
    }

    public PwElement(String elementName, IElement element) {
        super(elementName, element);
        this.driver = super.driver.castTo();
        this.element = driver.getLocator(locator);
    }

    public void setValue(String value) {
        element.fill(value);
        element.blur();
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
