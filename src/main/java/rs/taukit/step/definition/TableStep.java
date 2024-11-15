package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.playwright.PwTable;
import rs.taukit.step.manager.TableManager;

import static rs.taukit.utils.BasicUtils.*;

public class TableStep extends TableManager {

    /**
     * Проверяет таблицы на наличие и порядок столбцов
     * @param tableName название таблицы
     * @param dataTable таблица вида <table>
     *                  <tr><th>| Номер |</th><th>Название |</th></tr>
     * </table>, где <br><b>Номер</b> - порядковый номер столбца начинается с 1 (не обязательный столбец).
     *                  Если не указан - не будет проверяться.<br>
     *                  <b>Название</b> - название столбца.
     */
    @И("^в таблице \"(.+)\" проверить наличие столбцов:$")
    public void tableHeadersCheck(String tableName, DataTable dataTable) {
        super.tableHeadersCheck(tableName, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" нажать на \"(.+)\" в строке, соответствующей условиям:$")
    public void tableValueClick(String tableName, String elementName, DataTable dataTable) {
        element(tableName).castTo(PwTable.class).clickOnElementByConditions(elementName, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" нажать на \"(.+)\", сохранив его значение, согласно условиям$")
    public void tableValueSaveAndClick(String tableName, String elementName, DataTable conditions) {
        element(tableName).castTo(PwTable.class).saveAndClickOnElementByConditions(elementName, eval(conditions));
    }

    @И("^в таблице \"(.+)\" дождаться появления строки, соответствующей условиям:$")
    public void waitForTableConditions(String tableName, DataTable dataTable) {
        super.waitForTableConditions(tableName, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" найти строку, соответствующую условиям:$")
    public void findTableRowByConditions(String tableName, DataTable dataTable) {
        super.findRowByConditions(tableName, eval(dataTable));
    }

    @И("^в найденной строке найти и сохранить значения элементов:$")
    public void findTableRowByConditions(DataTable dataTable) {
        super.saveTableRowElementValue(eval(dataTable));
    }

    @И("^в найденной строке нажать на \"(.+)\"$")
    public void tableRowElementClick(String elementName) {
        super.clickOnElement(elementName);
    }

    @И("^в строке \\((.+)\\) нажать на \"(.+)\"$")
    public void tableRowElementClick(String rowName, String elementName) {
        ((IHasTableElements) scenarioData().get(rowName)).tableRowElementClick(rowName, elementName);
    }

}
