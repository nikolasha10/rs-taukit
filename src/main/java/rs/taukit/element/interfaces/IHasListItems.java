package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

import java.util.List;

public interface IHasListItems {
    List<? extends IElement> getItemList();
    IHasBlockElements findItemByConditions(DataTable dataTable);
}
