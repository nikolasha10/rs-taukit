package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

public interface IHasTableElements extends IElement {
    IElement getElementByConditions(String elementName, DataTable conditions);

    void clickOnElementByConditions(String elementName, DataTable conditions);

    void findTableRowByConditions(String rowName, DataTable dataTable);

    void tableRowSaveElementValues(String rowName, DataTable dataTable);

    void tableRowElementClick(String rowName, String elementName);
}
