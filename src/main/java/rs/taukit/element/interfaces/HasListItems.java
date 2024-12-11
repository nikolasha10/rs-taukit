package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

import java.util.List;

public interface HasListItems {
    List<? extends IElement> getItemList();

    IContainer findItemByConditions(DataTable dataTable);

    void selectItem(String itemName);

    int getSize();
}
