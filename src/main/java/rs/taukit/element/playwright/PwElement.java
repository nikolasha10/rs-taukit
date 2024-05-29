package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.Element;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.ElementNotBuiltException;
import rs.taukit.page.playwright.ElementBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.driver;
import static rs.taukit.utils.TextUtils.hexToRGB;

@Getter
public class PwElement extends Element {
    protected Locator elementPwLocator;
    protected PlaywrightDriver driver = driver().castTo();

    public PwElement(String elementName, Locator locator) {
        super(elementName, "");
        this.elementPwLocator = locator;
        buildDescendants();
    }

    public PwElement(String elementName, ElementModel model) {
        super(elementName, model);
        this.elementPwLocator = getPwLocator(searchMethodName, selector);
        buildDescendants();
    }

    public PwElement(String elementName, String selector) {
        this(elementName, new ElementModel(Map.of("локатор", selector)));
        buildDescendants();
    }

    public PwElement(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
        buildDescendants();
    }

    public PwElement(String elementName, Locator locator, ElementModel model) {
        super(elementName, model);
        this.elementPwLocator = locator;
        buildDescendants();
    }

    public PwElement(String elementName, IElement elementPwLocator) {
        super(elementName, elementPwLocator);
        this.elementPwLocator = getPwLocator(searchMethodName, selector);
        buildDescendants();
    }

    protected Locator getPwLocator(String searchMethodName, String selector) {
        PlaywrightDriver playwrightDriver = driver().castTo();
        Page page = playwrightDriver.getCurrentBrowser().getActivePwPage();
        return switch (searchMethodName) {
            case "test-id" -> page.getByTestId(selector);
            case "xpath" -> page.locator(selector);
            default -> throw ElementNotBuiltException.searchMethodNotFound(elementName, model).get();
        };
    }

    private void buildDescendants() {
        this.descendants = this.descendants.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            PwElement pwElement = entry.getValue().castTo(PwElement.class);
            Locator pwLocator = elementPwLocator.locator(pwElement.elementPwLocator);
            return ElementBuilder.buildElement(name, pwElement, pwLocator);
        }).collect(Collectors.toMap(IElement::getElementName, element -> element));
    }

    public void setValue(String value) {
        elementPwLocator.fill(value);
        elementPwLocator.blur();
    }

    @Override
    public boolean isVisible() {
        return elementPwLocator.count() < 2
                ? elementPwLocator.isVisible()
                : elementPwLocator.all().stream().anyMatch(Locator::isVisible);
    }

    @Override
    public boolean isEnabled() {
        return elementPwLocator.isEnabled();
    }

    @Override
    public void click() {
        elementPwLocator.click();
    }

    @Override
    public String getText() {
        try {
            return elementPwLocator.innerText();
        } catch (Exception e){
            String message = String.format("Не удалось получить значение элемента '%s'", elementName);
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public String getBgColor() {
        String jsExpression = "(element) => window.getComputedStyle(element).getPropertyValue('background-color')";
        return elementPwLocator.evaluate(jsExpression).toString();
    }

    @Override
    public boolean hasBgColor(String colorRGB) {
        return getBgColor().equalsIgnoreCase(hexToRGB(colorRGB));
    }

    @Override
    public boolean hasText(String value) {
        return getText().equals(value);
    }
}
