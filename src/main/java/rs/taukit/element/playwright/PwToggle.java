package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.Editable;

import java.util.LinkedHashMap;

public class PwToggle extends PwElement implements Editable {
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
        IElement clickable = getDescendant("Переключатель");
        boolean isTurnedOn = getDescendant("Включен").isVisible();
        boolean switchOn = value.equalsIgnoreCase("да");
        if (switchOn != isTurnedOn) {
            clickable.click();
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

    public boolean isToggledOn(){
        return getDescendant("Включен").isVisible();
    }

    @Override
    public String getText() {
        return isToggledOn() ? "да" : "нет";
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
