package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.IElement;

import java.util.LinkedHashMap;
import java.util.List;

import static rs.taukit.utils.BasicUtils.page;

public class PwDropdownList extends PwBlock implements Editable {
    protected PwList options;
    protected PwElement selectedValue;

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
        this.selectedValue = descendants.get("Текущее значение").castTo(PwElement.class);
    }

    @Override
    public void setValue(String value) {
        try {
            List<String> selectedValues = elementPwLocator.selectOption(value);
            if (!selectedValues.isEmpty()) return;
        } catch (Exception ignored) {
            // ignored
        }
        try {
            driver.waitForCondition(() -> {
                forceClick();
                return options.isVisible();
            });
        } catch (Exception e) {
            String message = String.format("Не удалось дождаться значений выпадающего списка '%s' по локатору: %s",
                    elementName, options.elementPwLocator);
            e.addSuppressed(new RuntimeException(message));
            throw e;
        }
        options.selectItem(value);
    }

    @Override
    public List<String> getOptions() {
        // Кликаем принудительно, игнорируя перекрывающие элементы
        elementPwLocator.click(new Locator.ClickOptions().setForce(true));

        try {
            // Ждём, пока список опций станет видимым
            driver.waitForCondition(() -> options.isVisible(), 2_000);
            return options.getValues();
        } finally {
            // Закрываем — тоже с force, если нужно
            elementPwLocator.click(new Locator.ClickOptions().setForce(true));
        }
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
        return selectedValue.isVisible() ? selectedValue.getText() : "";
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
