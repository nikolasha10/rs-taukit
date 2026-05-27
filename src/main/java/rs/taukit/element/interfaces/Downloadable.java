package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;

import java.io.File;

public interface Downloadable extends IElement {
    void clickAndDownloadFile(File file);

    void clickAndDownloadFiles(DataTable dataTable);

    boolean isDownloaded(String fileName);
}
