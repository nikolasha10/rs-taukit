package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

import java.io.File;

public interface Downloadable extends IElement {
    void click(File file);

    void click(DataTable dataTable);

    boolean isDownloaded(String fileName);
}
