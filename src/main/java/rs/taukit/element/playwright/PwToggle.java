package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TestDataUtils;

import java.util.LinkedHashMap;

public class PwToggle extends PwElement implements IHasEditableValue {
    public PwToggle(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwToggle(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwToggle(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    @Override
    public void setValue(String value) {
        boolean isTurnedOn = TestDataUtils.get(descendants, "Включен").isVisible();
        boolean switchOn = value.equalsIgnoreCase("да");
        if (switchOn != isTurnedOn) {
            click();
        }
    }

    @Override
    public void setValue(String value, String controlValue) {
        boolean switchOn = value.equalsIgnoreCase("да");
        driver.waitForCondition(() -> {
            setValue(value);
            return descendants.get("включен").isVisible() == switchOn;
        });
    }
}
