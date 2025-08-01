package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.TextFieldManager;

import static rs.taukit.utils.BasicUtils.eval;

public class TextFieldStep extends TextFieldManager {

    @И("^ввести в поле \"(.+)\" значение \"(.+)\"$")
    public void fill(String fieldName, String value) {
        super.setValue(fieldName, eval(value));
    }

    @И("^ввести в поле \"(.+)\" значение \"(.+)\" и проконтролировать$")
    public void fillWithControl(String fieldName, String value) {
        super.setValue(fieldName, eval(value), eval(value));
    }

    @И("^сохранить значения поля \"(.+)\", как \"(.+)\"$")
    public void saveElementValue(String fieldName, String parameterName) {
        super.saveElementValue(fieldName, eval(parameterName));
    }

    @И("^сохранить значения элементов в параметры:$")
    public void saveElementValue(DataTable dataTable) {
        super.saveElementsValue(eval(dataTable));
    }
}
