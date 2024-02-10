package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.Element;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.TextUtils.hexToRGB;

@Getter
public class PwElement extends Element {
    protected final PlaywrightDriver driver;
    protected Locator element;

    public PwElement(String elementName, Locator locator) {
        super(elementName, "");
        this.driver = super.driver.castTo();
        this.element = locator;
    }

    public PwElement(String elementName, ElementModel model) {
        super(elementName, model);
        this.driver = super.driver.castTo();
        this.element = getPwLocator(locator);
    }

    public PwElement(String elementName, String selector) {
        this(elementName, new ElementModel(Map.of("локатор", selector)));
    }

    public PwElement(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwElement(String elementName, Locator locator, ElementModel model) {
        this(elementName, locator);
    }

    public PwElement(String elementName, IElement element) {
        super(elementName, element);
        this.driver = super.driver.castTo();
        this.element = getPwLocator(locator);
    }

    protected Locator getPwLocator(String selector) {
        Page page = driver.getCurrentBrowser().getActivePwPage();
        return selector.contains("/") ? page.locator(selector) : page.getByTestId(selector);
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
    public boolean isEnabled() {
        return element.isEnabled();
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public String getText() {
        return element.innerText();
    }

    @Override
    public String getBgColor() {
        String jsExpression = "(element) => window.getComputedStyle(element).getPropertyValue('background-color')";
        return element.evaluate(jsExpression).toString();
    }

    @Override
    public boolean hasBgColor(String colorRGB) {
        TauLogger.console("getBgColor(): %s, hexToRGB(colorRGB): %s", getBgColor(), hexToRGB(colorRGB));
        return getBgColor().equalsIgnoreCase(hexToRGB(colorRGB));
    }
}
