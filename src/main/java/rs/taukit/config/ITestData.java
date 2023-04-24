package rs.taukit.config;

import io.cucumber.datatable.DataTable;

import java.util.Map;

public interface ITestData {
    public Map<String, DataTable> getDataTables();
}
