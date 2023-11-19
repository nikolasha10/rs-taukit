package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.page;

public class PwDropdownList extends PwElement implements IHasEditableValue {
    protected final Locator options;
    protected final Locator selectedValue;

    public PwDropdownList(String elementName, ElementModel model) {
        super(elementName, model);
        this.options = driver.getLocator(model.get("элементы"));
        this.selectedValue = driver.getLocator(model.get("значение"));
    }

    public PwDropdownList(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwDropdownList(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.options = driver.getLocator(model.get("элементы"));
        this.selectedValue = driver.getLocator(model.get("значение"));
    }

    @Override
    public void setValue(String value) {
        element.click();
        element.locator(options).filter(new Locator.FilterOptions().setHasText(Pattern.compile(value))).click();
    }

    @Override
    public void setValue(String value, String controlValue) {
        driver.waitForCondition(() -> {
            setValue(value);
            page().waitForTimeout(1000);
            return getText().equals(controlValue);
        });
    }

    @Override
    public String getText() {
        return element.locator(selectedValue).innerText();
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {

    }
}
