package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.database.DBDriver;
import rs.taukit.exceptions.DBExecuteSqlException;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.sql.SQLException;

public class AllureBasicDataBaseStep extends BasicUtils {
    private final String dataBaseName;
    private final DBDriver dbDriver;

    public AllureBasicDataBaseStep(String dataBaseName) {
        this.dataBaseName = dataBaseName;
        try {
            this.dbDriver = new DBDriver(dataBaseName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Step("в базе данных '{this.dataBaseName}' выполнить sql-скрипт из файла '{sqlFileName}'")
    public void execute(String sqlFileName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры sql-скрипта", dataTable);
        testContext().getScenarioData().put(dataTable);
        String sqlScript = eval(FileUtils.readFile(Config.getProperty("sql.files.dir") + sqlFileName));
        TauLogger.attach("sql-скрипт", sqlScript);
        try {
            int rowCount = dbDriver.execute(sqlScript);
            String rowsUpdated = rowCount >= 0 ? " Обновлено строк: " + rowCount : "";
            TauLogger.report("В базе данных \"%s\" выполнен sql-скрипт из файла \"%s\".%s",
                    dataBaseName, sqlFileName, rowsUpdated);
            System.out.println("Параметры sql-скрипта:");
            System.out.println(dataTable);
        } catch (SQLException e) {
            throw new DBExecuteSqlException(e, dataBaseName, sqlFileName);
        }
    }
}
