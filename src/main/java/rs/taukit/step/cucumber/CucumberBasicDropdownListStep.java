package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.step.BasicStory;
import rs.taukit.step.allure.AllureBasicDropdownListStep;
import rs.taukit.step.allure.AllureBasicTextFieldStep;
import rs.taukit.utils.TauLogger;

import static rs.taukit.step.utils.BasicUtils.eval;

public class CucumberBasicDropdownListStep {
    private AllureBasicDropdownListStep getDropdownList(String fieldName){
        return new AllureBasicDropdownListStep(fieldName);
    }
    @И("^в списке \"(.+)\" выбрать значение \"(.+)\"$")
    public void selectValue(String fieldName, String value) {
        getDropdownList(fieldName).selectValue(eval(value));
    }

}
