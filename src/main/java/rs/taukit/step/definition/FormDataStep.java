package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.FormDataManager;

import static rs.taukit.utils.BasicUtils.eval;

public class FormDataStep extends FormDataManager {

    @И("^заполнить поля формы значениями:$")
    public void fillFormFields(DataTable dataTable) {
        super.fillForm(eval(dataTable));
    }

    @И("^заполнить поля формы в контейнере \"(.+)\" \\((\\d+)\\) значениями:$")
    public void fillFormFieldsBlock(String blockName, int blockNumber, DataTable dataTable) {
        super.fillFormBlock(blockName, blockNumber, eval(dataTable));
    }

    @И("^заполнить поля формы в контейнере \"(.+)\" значениями:$")
    public void fillFormFieldsBlock(String blockName, DataTable dataTable) {
        super.fillFormBlock(blockName, eval(dataTable));
    }

}
