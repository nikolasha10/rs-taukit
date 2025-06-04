package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.HasText;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.utils.TimeStringParser;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.page;

public class PwSearchField extends PwBlock implements Editable {
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
        PwTextField textField = descendants.get("поле ввода").castTo(PwTextField.class);
        textField.getElementPwLocator().fill(value);
        if (value.equalsIgnoreCase("заполнить вручную")) {
            double searchValueTimeout = TimeStringParser.getTime(Config.getProperty("element.check.timeout"));
            PwButton fillManually = descendants.get("заполнить вручную").castTo(PwButton.class);
            driver.waitForCondition(fillManually::isVisible, searchValueTimeout);
            fillManually.click();
        } else {
            descendants.get("список значений").castTo(PwList.class).selectItem(value);
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
        String currentValueElementName = "текущее значение";
        PwElement value = descendants.get(currentValueElementName).castTo(PwElement.class);
        try {
            return value.castTo(HasText.class).getText();
        } catch (Exception e) {
            throw new AutoTestBrokenException("""
                    Не удалось получить значение элемента '%s'. Тип: %s. Потомок: %s. Локатор: %s""",
                    elementName, model.getType(), currentValueElementName, value.getElementPwLocator());
        }
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
