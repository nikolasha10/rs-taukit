package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.page;

public class PwDropdownList extends PwElement implements IHasEditableValue {
    protected Locator options;
    protected Locator selectedValue;

    public PwDropdownList(String elementName, ElementModel model) {
        super(elementName, model);
        initFields(model);
    }

    public PwDropdownList(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwDropdownList(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        initFields(model);
    }

    public PwDropdownList(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    private void initFields(ElementModel model) {
        this.options = getPwLocator(model.get("элементы"));
        this.selectedValue = getPwLocator(model.get("значение"));
    }

    @Override
    public void setValue(String value) {
        element.click();
        Locator optionsLocator = Optional.ofNullable(element.locator(options)).orElseThrow();
        optionsLocator.filter(new Locator.FilterOptions().setHasText(Pattern.compile(value))).first().click();
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
    public void click() {

    }
}
