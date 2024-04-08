package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasCheckbox;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwCheckbox extends PwElement implements IHasEditableValue, IHasCheckbox {
    private final PwElement checked;

    public PwCheckbox(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwCheckbox(String elementName, ElementModel model) {
        super(elementName, model);
        this.checked = getDescendant("Установлен");
    }

    public PwCheckbox(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
        this.checked = getDescendant("Установлен");
    }

    @Override
    public void setValue(String value, String controlValue) {
        driver.waitForCondition(() -> {
            setValue(value);
            return value.equalsIgnoreCase("да") == getValue();
        });
    }

    public boolean getValue() {
        return elementPwLocator.isVisible();
    }

    @Override
    public void setValue(String value) {
        IElement clickable = getDescendant("Чекбокс");
        boolean isTurnedOn = getDescendant("Установлен").isVisible();
        boolean switchOn = value.equalsIgnoreCase("да");
        if (switchOn != isTurnedOn) {
            clickable.click();
        }
    }

    @Override
    public void setCheckbox() {
        setValue("да");
    }

    @Override
    public void unsetCheckbox() {
        setValue("нет");
    }
}
