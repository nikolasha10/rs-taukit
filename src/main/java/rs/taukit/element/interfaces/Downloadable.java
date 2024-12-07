package rs.taukit.element.interfaces;

import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;

import java.io.File;

import static rs.taukit.utils.BasicUtils.scenario;

public interface Downloadable extends IElement {
    void click(String fileName);

    void click(DataTable dataTable);

    boolean isDownloaded(String fileName);
}
