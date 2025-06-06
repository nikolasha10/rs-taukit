package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.utils.TauLogger;

public class DictionaryManager extends StepManager {

    @Step("в справочнике '{fieldName}' выбрать значение '{value}")
    public void selectValue(String fieldName, String value) {
        run(() -> {
            Editable editable = getPage().getElement(fieldName);
            editable.setValue(value);
        }, "Не удалось выбрать значение '%s' в справочнике '%s'", value, fieldName);
        TauLogger.console("В справочнике '%s' выбрано значение '%s'", fieldName, value);
    }
}
