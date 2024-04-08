package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicCheckboxStep;

public class CucumberBasicCheckboxStep {
    private AllureBasicCheckboxStep checkbox(String fieldName) {
        return new AllureBasicCheckboxStep(fieldName);
    }

    @И("^установить чекбокс \"(.+)\"$")
    public void checkBox(String fieldName) {
        checkbox(fieldName).check();
    }

    @И("^снять чекбокс \"(.+)\"$")
    public void uncheckBox(String fieldName) {
        checkbox(fieldName).uncheck();
    }
}
