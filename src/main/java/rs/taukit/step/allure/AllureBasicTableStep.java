package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicTableStep extends BasicUtils {
    private final String tableName;
    private final IHasTableElements tableElement;

    public AllureBasicTableStep(String tableName) {
        this.tableName = tableName;
        Class<IElement> elementType = ElementType.getType("таблица").getElementClass();
        this.tableElement = page().replaceElement(tableName, elementType).castTo(IHasTableElements.class);
    }

    public AllureBasicTableStep(IHasTableElements tableElement) {
        this.tableName = tableElement.getElementName();
        this.tableElement = tableElement;
    }

    @Step("в таблице '{this.tableName}' нажать на '{elementName}' в строке, соответствующей условиям:")
    public void clickOnElementByConditions(String elementName, DataTable conditions) {
        TauLogger.attachDataTable("Условия выбора элемента", conditions);
        try {
            tableElement.clickOnElementByConditions(elementName, conditions);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на элемент '%s' в таблице '%s'", elementName, tableName);
        }
    }

    @Step("в таблице '{this.tableName}' найти строку, соответствующую условиям:")
    public void findRowByConditions(DataTable dataTable) {
        TauLogger.attach("Условия поиска строки", dataTable);
        try {
            int rowIndex = tableElement.findRowByConditions(dataTable);
            TauLogger.report("Найдена строка %d", rowIndex + 1);
        } catch (Exception e) {
            TauLogger.fail(e, "В таблице '%s' не удалось найти строку, соответствующую заданным условиям", tableName);
        }
    }

    @Step("в найденной строке сохранить значения элементов:")
    public void saveTableRowElementValue(DataTable dataTable) {
        DataTable resultTable = tableElement.tableRowSaveElementValues(dataTable);
        TauLogger.attachDataTable("Значения элементов", resultTable);
        System.out.println(resultTable);
    }

    @Step("в найденной строке нажать на '{elementName}'")
    public void clickOnElement(String elementName) {
        tableElement.clickOnElement(elementName);
        TauLogger.report("Нажали на элемент '%s' в таблице '%s'", elementName, tableName);
    }
}
