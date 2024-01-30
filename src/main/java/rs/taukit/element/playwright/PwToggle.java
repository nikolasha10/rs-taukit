package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwToggle extends PwElement implements IHasEditableValue {
    private final Locator turnedOn;

    public PwToggle(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
        this.turnedOn = getPwLocator(model.get("включен"));
    }

    public PwToggle(String elementName, ElementModel model) {
        super(elementName, model);
        this.turnedOn = getPwLocator(model.get("включен"));
    }
    public PwToggle(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.turnedOn = getPwLocator(model.get("включен"));
    }

    @Override
    public void setValue(String value) {
        boolean isTurnedOn = element.locator(turnedOn).isVisible();
        boolean switchOn = value.equalsIgnoreCase("да");
        if (switchOn != isTurnedOn) {
            element.click();
        }
    }

    @Override
    public void setValue(String value, String controlValue) {

    }
}
