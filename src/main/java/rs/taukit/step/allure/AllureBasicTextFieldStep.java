package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicTextFieldStep extends BasicUtils {
    private final String fieldName;
    private final IHasText textField;
    private final IHasEditableValue editableValue;

    public AllureBasicTextFieldStep(String fieldName) {
        this.fieldName = fieldName;
        this.textField = page().getElement(fieldName);
        this.editableValue = textField instanceof IHasEditableValue ? textField.castTo(IHasEditableValue.class) : null;
    }

    @Step("заполнить поле '{this.fieldName}' значением '{value}'")
    public void setValue(String value) {
        editableValue.setValue(value);
        TauLogger.report("Поле \"%s\" заполнено значением \"%s\"", fieldName, textField.getText());
    }

    @Step("заполнить поле '{this.fieldName}' значением '{value}'")
    public void setValue(String value, String controlValue) {
        editableValue.setValue(value, controlValue);
        TauLogger.report("Поле \"%s\" заполнено значением \"%s\"", fieldName, textField.getText());
    }

    @Step("сохранить значения поля '{this.fieldName}', как '{parameterName}'")
    public void saveElementValue(String parameterName) {
        scenarioData().put(parameterName, textField.getText());
        TauLogger.report("Значение поля '%s' (%s) сохранено, как '%s'",
                this.fieldName, scenarioData().get(parameterName), parameterName);
    }
}
