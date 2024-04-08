package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicCheckboxStep extends BasicUtils {
    private final String fieldName;
    private final IHasEditableValue checkbox;

    public AllureBasicCheckboxStep(String fieldName) {
        this.fieldName = fieldName;
        this.checkbox = page().getElement(fieldName);
    }

    @Step("установить чекбокс '{this.fieldName}'")
    public void check() {
        checkbox.setValue("да");
        TauLogger.report("Установлен чекбокс \"%s\"", fieldName);
    }

    @Step("снять чекбокс '{this.fieldName}'")
    public void uncheck() {
        checkbox.setValue("нет");
        TauLogger.report("Снят чекбокс \"%s\"", fieldName);
    }
}
