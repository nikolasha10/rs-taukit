package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

public interface IHasDownloadableFile extends IElement {
    void click(String fileName);

    void click(DataTable dataTable);
}
