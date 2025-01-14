package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Clickable;
import rs.taukit.element.interfaces.CanBeChecked;
import rs.taukit.element.interfaces.Editable;

import java.util.LinkedHashMap;

public class PwCheckbox extends PwElement implements Editable, CanBeChecked {
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
            return getText().equalsIgnoreCase("да") == isChecked();
        });
    }

    @Override
    public boolean isChecked() {
        return this.checked.isVisible();
    }

    @Override
    public void setValue(String value) {
        Clickable clickable = getDescendant("Чекбокс");
        boolean switchOn = value.equalsIgnoreCase("да");
        if (switchOn != isChecked()) {
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

    @Override
    public String getText() {
        return isChecked() ? "да" : "нет";
    }

    @Override
    public boolean hasText(String expectedValue) {
        return getText().equals(expectedValue);
    }

    @Override
    public Boolean containsText(String expectedText) {
        return null;
    }
}
