package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.TimeoutError;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Clickable;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.Selectable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static rs.taukit.utils.BasicUtils.page;

public class PwDropdownList extends PwBlock implements Editable, Selectable {
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
                elementPwLocator.evaluate("el => el.scrollIntoView({ behavior: 'smooth', block: 'center' })");
                try {
                    forceClick();
                } catch (Exception e) {
                    this.exception = new RuntimeException(String.format("Не удалось нажать на поле '%s'", elementName), e);
                    return false;
                }
                try {
                    return options.isVisible() && options.getItem(value).isVisible();
                } catch (Exception e) {
                    this.exception = new RuntimeException(String.format("Не удалось найти значение '%s' в выпадающем списке '%s'", value, elementName), e);
                    return false;
                }
            }, Config.getTimeProperty("conditions.wait.timeout"));
        } catch (TimeoutError te) {
            if (exception != null) {
                te.addSuppressed(this.exception);
            }
            throw te;
        }
        options.selectItem(value);
    }

    @Override
    public List<String> getOptions() {
        // Кликаем принудительно, игнорируя перекрывающие элементы
        this.forceClick();
        try {
            // Ждём, пока список опций станет видимым
            driver.waitForCondition(() -> options.isVisible());
            return options.getValues().stream().filter(Objects::nonNull).toList();
        } finally {
            // Закрываем — тоже с force, если нужно
            this.forceClick();
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
    public Clickable getOptionByNumber(int number) {
        try {
            driver.waitForCondition(() -> {
                elementPwLocator.evaluate("el => el.scrollIntoView({ behavior: 'smooth', block: 'center' })");
                try {
                    forceClick();
                } catch (Exception e) {
                    this.exception = new RuntimeException(String.format("Не удалось нажать на поле '%s'", elementName), e);
                    return false;
                }
                try {
                    return options.isVisible() && options.getSize() >= number;
                } catch (Exception e) {
                    this.exception = new RuntimeException(String.format(
                            "В выпадающем списке '%s' отсутствует %d-й элемент", elementName, number), e);
                    return false;
                }
            }, Config.getTimeProperty("conditions.wait.timeout"));
        } catch (TimeoutError te) {
            if (exception != null) {
                te.addSuppressed(this.exception);
            }
            throw te;
        }
        return options.get(number - 1);
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
