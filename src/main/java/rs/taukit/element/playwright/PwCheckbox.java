package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwCheckbox extends PwElement implements IHasEditableValue {
    private final Locator checked;

    public PwCheckbox(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwCheckbox(String elementName, ElementModel model) {
        super(elementName, model);
        this.checked = getDriver().getLocator(model.get("установлен"));
    }

    public PwCheckbox(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.checked = getDriver().getLocator(model.get("установлен"));
    }

    @Override
    public void setValue(String value, String controlValue) {
        driver.waitForCondition(() -> {
            setValue(value);
            return value.equalsIgnoreCase("да") == getValue();
        });
    }

    public boolean getValue() {
        return element.locator(checked).isVisible();
    }

    @Override
    public void setValue(String value) {
        boolean isTurnedOn = element.locator(checked).isVisible();
        boolean switchOn = value.equalsIgnoreCase("да");
        if (switchOn != isTurnedOn) {
            element.click();
        }
    }
}
