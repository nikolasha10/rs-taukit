package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static rs.taukit.utils.BasicUtils.page;

public class PwRadioButton extends PwElement implements IHasEditableValue {
    public PwRadioButton(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwRadioButton(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwRadioButton(String elementName, IElement element) {
        super(elementName, element);
    }

    public PwRadioButton(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    private List<PwBlock> getButtonList() {
        return descendants.get("Кнопки").castTo(PwList.class).getItemList();
    }

    @Override
    public void setValue(String value) {
        getButtonList().stream()
                .filter(item -> item.getElementByName("Значение").hasText(value))
                .findFirst().orElseThrow(valueNotFoundException(value))
                .click();
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    private Supplier<RuntimeException> valueNotFoundException(String value) {
        List<String> availableValues = getButtonList().stream()
                .map(item -> item.getElementByName("Значение").getText()).toList();
        String message = String.format("""
                        Не удалось найти значение '%s' среди значений радио-кнопки '%s' на странице '%s'
                        Доступны значения: %s""",
                value, elementName, page().getPageName(), availableValues);
        return () -> new RuntimeException(message);
    }
}
