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
    protected final Locator textField;
    protected final Locator values;
    protected final Locator fillManuallyButton;

    public PwSearchField(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }

    public PwSearchField(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwSearchField(String elementName, ElementModel model) {
        super(elementName, model);
        this.textField = driver.getLocator(model.get("поле ввода"));
        this.values = driver.getLocator(model.get("список значений"));
        this.fillManuallyButton = driver.getLocator(model.get("заполнить вручную"));
    }

    public PwSearchField(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.textField = driver.getLocator(model.get("поле ввода"));
        this.values = driver.getLocator(model.get("список значений"));
        this.fillManuallyButton = driver.getLocator(model.get("заполнить вручную"));
    }

    public PwSearchField(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    @Override
    public void setValue(String value) {
        element.locator(textField).fill(value);
        if (value.equalsIgnoreCase("заполнить вручную")) {
            double searchValueTimeout = TimeStringParser.getTime(Config.getProperty("searchValueTimeout"));
            driver.waitForCondition(() -> element.locator(fillManuallyButton).isVisible(), searchValueTimeout);
            element.locator(fillManuallyButton).click();
        } else {
            element.locator(values).filter(new Locator.FilterOptions().setHasText(value)).click();
        }
    }

    @Override
    public void setValue(String value, String controlValue) {
        driver.waitForCondition(() -> {
            super.setValue(value);
            page().waitForTimeout(1000);
            String currentValue = element.inputValue();
            return currentValue.equals(controlValue);
        });
    }

    @Override
    public String getText() {
        return element.inputValue();
    }


}
