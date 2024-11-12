package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;

public class ToggleManager extends StepManager {
    @Step("включить переключатель '{fieldName}'")
    public void turnOn(String fieldName) {
        run(() -> {
            IHasEditableValue toggle = getPage().getElement(fieldName);
            toggle.setValue("да");
        }, "Не удалось включить переключатель '%s'", fieldName);
        TauLogger.report("Включен переключатель'%s'", fieldName);
    }

    @Step("выключить переключатель '{fieldName}'")
    public void turnOff(String fieldName) {
        run(() -> {
            IHasEditableValue toggle = getPage().getElement(fieldName);
            toggle.setValue("нет");
        }, "Не удалось выключить переключатель '%s'", fieldName);
        TauLogger.report("Выключен переключатель '%s'", fieldName);
    }
}
