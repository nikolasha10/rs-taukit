package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicTextFieldStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicTextFieldStep {
    private AllureBasicTextFieldStep getTextField(String fieldName) {
        return new AllureBasicTextFieldStep(fieldName);
    }

    @И("^ввести в поле \"(.+)\" значение \"(.+)\"$")
    public void fill(String fieldName, String value) {
        getTextField(fieldName).setValue(eval(value));
    }

    @И("^ввести в поле \"(.+)\" значение \"(.+)\" и проконтролировать$")
    public void fillWithControl(String fieldName, String value) {
        getTextField(fieldName).setValue(eval(value), eval(value));
    }

    @И("^сохранить значения поля \"(.+)\", как \"(.+)\"$")
    public void saveElementValue(String fieldName, String parameterName) {
        getTextField(fieldName).saveElementValue(parameterName);
    }
}
