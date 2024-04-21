package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.database.DBDriver;
import rs.taukit.exceptions.DBExecuteSqlException;
import rs.taukit.utils.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class AllureBasicDataBaseStep extends BasicUtils {
    private String dataBaseName;
    private DBDriver dbDriver;

    public AllureBasicDataBaseStep(String dataBaseName) {
        this.dataBaseName = dataBaseName;
        try {
            this.dbDriver = new DBDriver(dataBaseName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public AllureBasicDataBaseStep() {
    }

    private static Runnable getRunnable(DataTable dataTable, List<List<String>> resultList) {
        Runnable runnable = () -> {
            ResultSet resultSet = scenarioData().get("sqlQueryResultSet");
            try {
                resultSet.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            dataTable.asMaps().forEach(map -> {
                String fieldName = TestDataUtils.get(map, "Поле");
                String parameterName = TestDataUtils.get(map, "Параметр");
                String fieldValue = "null";
                try {
                    fieldValue = resultSet.getString(fieldName);
                } catch (SQLException ignored) {
                    // ignored
                }
                scenarioData().put(parameterName, fieldValue);
                resultList.add(Arrays.asList(fieldName, fieldValue, parameterName));
            });
        };
        runnable.run();
        return runnable;
    }

    @Step("в базе данных '{this.dataBaseName}' выполнить sql-скрипт из файла '{sqlFileName}'")
    public void execute(String sqlFileName) {
        String sqlScript = eval(FileUtils.readFile(Config.getProperty("sql.files.dir") + sqlFileName));
        TauLogger.attach("sql-скрипт", sqlScript);
        try {
            int rowCount = dbDriver.execute(sqlScript);
            String rowsUpdated = rowCount > 0 ? " Обновлено строк: " + rowCount : "";
            TauLogger.report("В базе данных \"%s\" выполнен sql-скрипт из файла \"%s\".%s",
                    dataBaseName, sqlFileName, rowsUpdated);
        } catch (SQLException e) {
            throw new DBExecuteSqlException(e, dataBaseName, sqlFileName);
        }
    }

    @Step("в базе данных '{this.dataBaseName}' выполнить sql-скрипт из файла '{sqlFileName}'")
    public void execute(String sqlFileName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры sql-скрипта", dataTable);
        scenarioStorage().getScenarioData().put(dataTable);
        String sqlScript = eval(FileUtils.readFile(Config.getProperty("sql.files.dir") + sqlFileName));
        TauLogger.attach("sql-скрипт", sqlScript);
        Supplier<Integer> supplier = () -> {
            try {
                return dbDriver.execute(sqlScript);
            } catch (SQLException e) {
                throw new DBExecuteSqlException(e, dataBaseName, sqlFileName);
            }
        };
        int rowCount = supplier.get();
        scenarioStorage().saveStepAction(supplier::get);
        String rowsUpdated = rowCount > 0 ? " Обновлено строк: " + rowCount : "";
        TauLogger.report("В базе данных \"%s\" выполнен sql-скрипт из файла \"%s\".%s",
                dataBaseName, sqlFileName, rowsUpdated);
        TauLogger.console("Параметры sql-скрипта:", dataTable);
    }

    @Step("сохранить результат выполнения sql-скрипта в параметры:")
    public void saveSqlResultSet(DataTable dataTable) {
        List<List<String>> resultList = new ArrayList<>();
        resultList.add(Arrays.asList("Поле", "Значение", "Параметр"));
        Runnable runnable = getRunnable(dataTable, resultList);
        scenarioStorage().saveStepAction(runnable);
        DataTable resultDataTable = DataTable.create(resultList, TextUtils.tableConverter);
        TauLogger.console("Результаты выполнения sql-скрипта сохранены в параметры:", resultDataTable);
        TauLogger.attachDataTable("Результаты выполнения sql-скрипта сохранены в параметры", resultDataTable);
    }
}
