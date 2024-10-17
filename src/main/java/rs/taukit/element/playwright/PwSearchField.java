package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TimeStringParser;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.page;

public class PwSearchField extends PwElement implements IHasEditableValue {
    public PwSearchField(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }

    public PwSearchField(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwSearchField(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwSearchField(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwSearchField(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    @Override
    public void setValue(String value) {
        descendants.get("поле ввода").castTo(PwTextField.class).setValue(value);
        if (value.equalsIgnoreCase("заполнить вручную")) {
            double searchValueTimeout = TimeStringParser.getTime(Config.getProperty("element.check.timeout"));
            PwButton fillManually = descendants.get("заполнить вручную").castTo(PwButton.class);
            driver.waitForCondition(fillManually::isVisible, searchValueTimeout);
            fillManually.click();
        } else {
            descendants.get("список значений").castTo(PwList.class).setValue(value);
        }
    }

    @Override
    public void setValue(String value, String controlValue) {
        driver.waitForCondition(() -> {
            super.setValue(value);
            page().waitForTimeout(1000);
            String currentValue = elementPwLocator.inputValue();
            return currentValue.equals(controlValue);
        });
    }

    @Override
    public String getText() {
        return elementPwLocator.inputValue();
    }


}
