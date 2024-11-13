package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.utils.TauLogger;

public class TextFieldManager extends StepManager {

    @Step("заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String fieldName, String value) {
        run(() -> {
            IHasEditableValue editableValue = getPage().getElement(fieldName);
            editableValue.setValue(value);
        }, "Не удалось заполнить поле '%s' значением '%s'", fieldName, value);
        TauLogger.report("Поле '%s' заполнено значением '%s'", fieldName, value);
    }

    @Step("заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String fieldName, String value, String controlValue) {
        run(() -> {
            IHasEditableValue editableValue = getPage().getElement(fieldName);
            editableValue.setValue(value, controlValue);
        }, "Не удалось заполнить поле '%s' значением '%s'", fieldName, value);
        TauLogger.report("Поле '%s' заполнено значением '%s'", fieldName, value);
    }

    @Step("сохранить значения поля '{fieldName}', как '{parameterName}'")
    public void saveElementValue(String fieldName, String parameterName) {
        run(() -> {
            IHasText textField = getPage().getElement(fieldName);
            scenarioData.put(parameterName, textField.getText());
        }, "Не удалось сохранить значение поля '%s' в параметр '%s'", fieldName, parameterName);
        TauLogger.report("Значение поля '%s' (%s) сохранено, как '%s'",
                fieldName, scenarioData.get(parameterName), parameterName);
    }
}
