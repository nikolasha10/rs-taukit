package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicDropdownListStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicDropdownListStep {
    private AllureBasicDropdownListStep dropdownList(String fieldName) {
        return new AllureBasicDropdownListStep(fieldName);
    }

    @И("^в списке \"(.+)\" выбрать значение \"(.+)\"$")
    public void selectValue(String fieldName, String value) {
        dropdownList(fieldName).selectValue(eval(value));
    }

}
