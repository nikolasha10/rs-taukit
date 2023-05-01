package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.AElement;
import rs.taukit.element.IHasEditableValue;

import java.util.LinkedHashMap;

public class ElementList extends AElement implements IHasEditableValue {
    private final String elementName;
    @Getter
    private final LinkedHashMap<String, String> elementObject;
    @Getter
    private final String locator;
    private final Locator element;
    private final Locator buttons;
    private final Locator labels;

    public ElementList(String elementName, LinkedHashMap<String, String> elementObject) {
        PlaywrightDriver driver = (PlaywrightDriver) DriverManager.getInstance().getDriver();
        this.elementObject = elementObject;
        this.elementName = elementName;
        this.locator = elementObject.get("Локатор");
        this.element = driver.getLocator(locator);
        this.buttons = driver.getLocator(elementObject.get("Элементы списка"));
        this.labels = driver.getLocator(elementObject.get("Названия элементов"));
    }

    @Override
    public String setValue(String value) {
        element.filter(new Locator.FilterOptions().setHas(labels))
                .filter(new Locator.FilterOptions().setHas(buttons))
                .click();
        return getValue();
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {

    }
}
