package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicRadioButtonStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicRadioButtonStep {
    private AllureBasicRadioButtonStep radio(String fieldName) {
        return new AllureBasicRadioButtonStep(fieldName);
    }

    @И("^установить переключатель \"(.+)\" в значение \"(.+)\"$")
    public void setRadioButton(String blockName, String fieldName, String value) {
        radio(fieldName).switchRadio(eval(value));
    }

}
