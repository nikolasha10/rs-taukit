package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.element.playwright.PwList;
import rs.taukit.step.BasicStory;
import rs.taukit.step.allure.AllureBasicFormDataStep;
import rs.taukit.step.allure.AllureBasicTextFieldStep;

import static rs.taukit.step.utils.BasicUtils.*;

public class CucumberBasicFormDataStep {
    private AllureBasicFormDataStep getFormData (DataTable dataTable){
        return new AllureBasicFormDataStep(dataTable, page());
    }
    @И("^заполнить поля формы значениями:$")
    public void fillFormFields(DataTable dataTable) {
        getFormData(eval(dataTable)).fillForm();
    }
    @И("^заполнить поля формы в блоке \"(.+)\" \\((\\d+)\\) значениями:$")
    public void fillFormFieldsBlock(String blockName, int blockNumber, DataTable dataTable) {
        getFormData(eval(dataTable)).fillFormBlock(blockName, blockNumber);
    }

}
