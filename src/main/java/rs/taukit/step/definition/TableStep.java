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
     *
     * @param tableName название таблицы
     * @param dataTable таблица вида <table>
     *                  <tr><th>| Номер |</th><th>Название |</th></tr>
     *                  </table>, где <br><b>Номер</b> - порядковый номер столбца начинается с 1 (не обязательный столбец).
     *                  Если не указан - не будет проверяться.<br>
     *                  <b>Название</b> - название столбца.
     */
    @И("^в таблице \"(.+)\" проверить наличие столбцов:$")
    public void tableHeadersCheck(String tableName, DataTable dataTable) {
        super.tableHeadersCheck(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" для каждого заголовка столбца проверить выполнение условий:$")
    public void tableHeadersCheckConditions(String tableName, DataTable dataTable) {
        super.tableHeadersCheckConditions(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" для каждой строки проверить выполнение условий:$")
    public void tableAllRowsCheckConditions(String tableName, DataTable dataTable) {
        super.tableAllRowsCheckConditions(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" для каждой строки проверить выполнение любого из условий:$")
    public void tableAllRowsCheckAnyCondition(String tableName, DataTable dataTable) {
        super.tableAllRowsCheckAnyCondition(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" для каждой ячейки в столбце № (\\d+) проверить выполнение условий:$")
    public void tableColumnCellsCheckConditions(String tableName, int columnNumber, DataTable dataTable) {
        super.tableColumnCellsCheckConditions(eval(tableName), columnNumber, eval(dataTable));
    }

    /**
     * Устанавливает заголовок столбца таблицы в качестве текущего контекста
     *
     * @param tableName    название таблицы
     * @param headerNumber номер столбца
     */
    @И("^в таблице \"(.+)\" перейти к заголовку столбца № (\\d+)$")
    public void tableHeaderSetContext(String tableName, int headerNumber) {
        super.tableHeaderSetContext(eval(tableName), headerNumber);
    }

    /**
     * Устанавливает заголовок столбца таблицы в качестве текущего контекста
     *
     * @param tableName  название таблицы
     * @param headerName название столбца
     */
    @И("^в таблице \"(.+)\" перейти к заголовку столбца \"(.+)\"$")
    public void tableHeaderSetContext(String tableName, String headerName) {
        super.tableHeaderSetContext(eval(tableName), headerName);
    }

    @И("^в таблице \"(.+)\" для заголовков столбцов \"([\\d\\,\\-]+)\" проверить выполнение условий:$")
    public void tableHeadersCheckConditions(String tableName, String headersNumbers, DataTable dataTable) {
        super.tableHeadersCheckConditions(eval(tableName), eval(headersNumbers), eval(dataTable));
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
        super.waitForTableConditions(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" дождаться появления строки, соответствующей любому из условий:$")
    public void waitForTableAnyConditions(String tableName, DataTable dataTable) {
        super.waitForTableAnyConditions(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" дождаться отсутствия строки, соответствующей условиям:$")
    public void waitForTableNotContainsRow(String tableName, DataTable dataTable) {
        super.waitForTableNotContainsRow(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" дождаться появления строки, соответствующей условиям \\(в течение (.+)\\):$")
    public void waitForTableConditions(String tableName, String timeoutString, DataTable dataTable) {
        super.waitForTableConditions(eval(tableName), timeoutString, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" дождаться отсутствия строки, соответствующей условиям \\(в течение (.+)\\):$")
    public void waitForTableNotContainsRow(String tableName, String timeoutString, DataTable dataTable) {
        super.waitForTableNotContainsRow(eval(tableName), timeoutString, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" найти строку, соответствующую условиям:$")
    public void findTableRowByConditions(String tableName, DataTable dataTable) {
        super.findRowByConditions(eval(tableName), eval(dataTable));
    }

    @И("^в таблице \"(.+)\" подсчитать количество строк, соответствующих условиям, и сохранить, как \"(.+)\":$")
    public void countRowsByConditions(String tableName, String parameterName, DataTable dataTable) {
        super.countRowsByConditions(eval(tableName), parameterName, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" пролистать содержимое (вниз|вверх)$")
    public void scroll(String tableName, String direction) {
        super.scroll(eval(tableName), direction);
    }

    @И("^в найденной строке найти и сохранить значения элементов:$")
    public void findTableRowByConditions(DataTable dataTable) {
        super.saveTableRowElementValue(eval(dataTable));
    }

    @И("^в найденной строке нажать на \"(.+)\"$")
    public void tableRowElementClick(String elementName) {
        super.clickOnElement(eval(elementName));
    }

    @И("^в найденной строке (установить|снять) чекбокс \"(.+)\"$")
    public void tableRowCheckboxSet(String action, String checkboxName) {
        super.setCheckbox(checkboxName, action);
    }

    @И("^в найденной строке перейти к столбцу \"(.+)\"$")
    public void tableRowSetContext(String headerName) {
        super.tableRowSetContext(headerName);
    }

    @И("^в найденной строке перейти к контейнеру \"(.+)\"$")
    public void tableRowSetContainerContext(String containerName) {
        super.tableRowSetContainerContext(containerName);
    }

    @И("^в найденной строке в меню \"(.+)\" выбрать \"([^\"]+)\"$")
    public void tableRowSelectMenuItem(String menuName, String itemName) {
        super.tableRowSelectMenuItem(menuName, itemName);
    }

    @И("^в найденной строке в меню \"(.+)\" выбрать \"([^\"]+)\" и скачать файл \"([^\"]+)\"$")
    public void tableRowSelectMenuDownloadFile(String menuName, String itemName, String fileName) {
        super.tableRowSelectMenuDownloadFile(menuName, itemName, fileName);
    }

    @И("^в строке \\((.+)\\) нажать на \"(.+)\"$")
    public void tableRowElementClick(String rowName, String elementName) {
        ((IHasTableElements) scenarioData().get(rowName)).tableRowElementClick(rowName, elementName);
    }

}
