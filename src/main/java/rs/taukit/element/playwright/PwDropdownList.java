package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

import static rs.taukit.utils.BasicUtils.page;

public class PwDropdownList extends PwElement implements IHasEditableValue {
    protected PwList options;
    protected PwText selectedValue;

    public PwDropdownList(String elementName, ElementModel model) {
        super(elementName, model);
        initFields(model);
    }

    public PwDropdownList(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwDropdownList(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
        initFields(model);
    }

    public PwDropdownList(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    private void initFields(ElementModel model) {
        this.options = descendants.get("Значения").castTo(PwList.class);
        this.selectedValue = descendants.get("Текущее значение").castTo(PwText.class);
    }

    @Override
    public void setValue(String value) {
        driver.waitForCondition(() -> {
            click();
            return options.isVisible();
        });
        options.setValue(value);
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
        return selectedValue.getText();
    }

}
