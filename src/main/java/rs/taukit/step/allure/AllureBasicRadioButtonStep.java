package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicRadioButtonStep extends BasicUtils {
    private final String fieldName;
    private final IHasEditableValue editable;

    public AllureBasicRadioButtonStep(String fieldName) {
        this.fieldName = fieldName;
        this.editable = page().getElement(fieldName);
    }

    @Step("установить переключатель '{this.fieldName}' в значение '{value}'$")
    public void switchRadio(String value) {
        editable.setValue(value);
        TauLogger.report("Переключатель '%s' установлен в значение '%s'", fieldName, value);
    }
}
