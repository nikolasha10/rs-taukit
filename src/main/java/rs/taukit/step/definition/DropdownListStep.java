package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.DropdownListManager;

import static rs.taukit.utils.BasicUtils.eval;

public class DropdownListStep extends DropdownListManager {

    @И("^в выпадающем списке \"(.+)\" выбрать \"(.+)\"$")
    public void selectValue(String elementName, String value) {
        super.selectValue(elementName, eval(value));
    }

    /**
     * Проверить, что в выпадающем списке отображаются указанные значения (через таблицу)
     */
    @И("^проверить,что в выпадающем списке \"(.+)\" отображаются значения:$")
    public void verifyDropdownOptions(String elementName, DataTable dataTable) {
        super.verifyDropdownOptions(elementName, eval(dataTable));
    }
}