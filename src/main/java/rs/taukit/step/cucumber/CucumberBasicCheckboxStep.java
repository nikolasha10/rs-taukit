package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicCheckboxStep;

public class CucumberBasicCheckboxStep {
    private AllureBasicCheckboxStep checkbox(String fieldName) {
        return new AllureBasicCheckboxStep(fieldName);
    }

    @И("^установить чекбокс \"(.+)\"$")
    public void setCheckbox(String fieldName) {
        checkbox(fieldName).setValue();
    }
}
