package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicCheckboxStep extends BasicUtils {
    private final String fieldName;
    private final IHasEditableValue checkbox;

    public AllureBasicCheckboxStep(String fieldName) {
        this.fieldName = fieldName;
        Class<IElement> elementType = ElementType.getType("чекбокс").getElementClass();
        this.checkbox = page().replaceElement(fieldName, elementType).castTo(IHasEditableValue.class);
    }

    @Step("установить чекбокс '{this.fieldName}'")
    public void setValue() {
        checkbox.setValue("да");
        TauLogger.report("Установлен чекбокс '%s'", fieldName);
    }
}
