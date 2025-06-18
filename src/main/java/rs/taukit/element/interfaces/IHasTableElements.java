package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

import java.util.List;

public interface IHasTableElements extends IElement {
    int findRowByConditions(DataTable dataTable);

    IElement getElementByConditions(String elementName, DataTable conditions);

    void clickOnElementByConditions(String elementName, DataTable conditions);

    void clickOnElement(String elementName);

    void findTableRowByConditions(String rowName, DataTable dataTable);

    DataTable tableRowSaveElementValues(DataTable dataTable);

    void tableRowElementClick(String rowName, String elementName);

    boolean checkHeader(String name);

    int getHeaderIndex(String name);

    List<String> getHeaderNamesList();

    HasListItems getHeadersList();

    void setContext(String headerName);

    void setContainerContext(String containerName);

    IElement getActiveCell(String headerName);

    IContainer getHeaderByName(String headerName);

    List<IContainer> getRowList();

    void setCheckbox(String checkboxName);

    IContainer setActiveRow(int index);
}
