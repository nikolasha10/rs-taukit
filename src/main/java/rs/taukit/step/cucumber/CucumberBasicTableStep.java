package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.playwright.PwTable;
import rs.taukit.step.allure.AllureBasicTableStep;

import static rs.taukit.utils.BasicUtils.*;

public class CucumberBasicTableStep {
    AllureBasicTableStep tableStep(String tableName) {
        return new AllureBasicTableStep(tableName);
    }

    AllureBasicTableStep tableStep(IHasTableElements tableElement) {
        return new AllureBasicTableStep(tableElement);
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
        tableStep(tableName).waitForTableConditions(eval(dataTable));
    }

    @И("^в таблице \"(.+)\" найти строку \\((.+)\\), соответствующую условиям:$")
    public void findTableRowByConditions(String tableName, String rowName, DataTable dataTable) {
        element(tableName).castTo(IHasTableElements.class).findTableRowByConditions(rowName, eval(dataTable));
    }

    @И("^в таблице \"(.+)\" найти строку, соответствующую условиям:$")
    public void findTableRowByConditions(String tableName, DataTable dataTable) {
        tableStep(tableName).findRowByConditions(eval(dataTable));
    }

    @И("^в найденной строке найти и сохранить значения элементов:$")
    public void findTableRowByConditions(DataTable dataTable) {
        IHasTableElements currentTable = scenarioData().get("контекст таблицы");
        tableStep(currentTable).saveTableRowElementValue(eval(dataTable));
    }

    @И("^в найденной строке нажать на \"(.+)\"$")
    public void tableRowElementClick(String elementName) {
        IHasTableElements currentTable = scenarioData().get("контекст таблицы");
        tableStep(currentTable).clickOnElement(elementName);
    }

    @И("^в строке \\((.+)\\) нажать на \"(.+)\"$")
    public void tableRowElementClick(String rowName, String elementName) {
        ((IHasTableElements) scenarioData().get(rowName)).tableRowElementClick(rowName, elementName);
    }

}
