package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.utils.TauLogger;

public class DropdownListManager extends StepManager {

    @Step("в списке '{elementName}' выбрать значение '{value}'")
    public void selectValue(String elementName, String value) {
        run(() -> getPage().getElement(elementName).castTo(Editable.class).setValue(value),
                "Не удалось выбрать значение '%s' в выпадающем списке '%s'", value, elementName);
        TauLogger.report("В выпадающем списке '%s' выбрано значение '%s'", elementName, value);
    }
}
