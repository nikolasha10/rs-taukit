package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicToggleStep extends BasicUtils {
    private final String fieldName;
    private final IHasEditableValue toggle;

    public AllureBasicToggleStep(String fieldName) {
        this.fieldName = fieldName;
        this.toggle = page().getElement(fieldName);
    }

    @Step("включить переключатель '{this.fieldName}'")
    public void turnOn() {
        toggle.setValue("да");
        TauLogger.report("Включен переключатель\"%s\"", fieldName);
    }

    @Step("выключить переключатель '{this.fieldName}'")
    public void turnOff() {
        toggle.setValue("нет");
        TauLogger.report("Выключен переключатель \"%s\"", fieldName);
    }
}
