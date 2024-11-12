package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.database.DBDriver;
import rs.taukit.exceptions.DBExecuteSqlException;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static rs.taukit.utils.BasicUtils.eval;

public class DBManager extends StepManager {
    private String dataBaseName;
    private DBDriver dbDriver;

    private void init(String dataBaseName) {
        this.dataBaseName = dataBaseName;
        try {
            this.dbDriver = new DBDriver(dataBaseName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int execute(String sqlScript, String sqlFileName) {
        try {
            return dbDriver.execute(sqlScript);
        } catch (SQLException e) {
            throw new DBExecuteSqlException(e, dataBaseName, sqlFileName);
        }

    }

    @Step("в базе данных '{dataBaseName}' выполнить sql-скрипт из файла '{sqlFileName}'")
    public void executeScriptFromFile(String dataBaseName, String sqlFileName) {
        init(dataBaseName);
        String sqlScript = eval(FileUtils.readFile(Config.getProperty("sql.files.dir") + sqlFileName));
        TauLogger.attach("sql-скрипт", sqlScript);
        AtomicInteger count = new AtomicInteger(0);
        run(() -> count.set(execute(sqlScript, sqlFileName)));
        String rowsUpdated = count.get() > 0 ? " Обновлено строк: " + count.get() : "";
        TauLogger.report("В базе данных '%s' выполнен sql-скрипт из файла '%s'.%s", dataBaseName, sqlFileName, rowsUpdated);
    }

    @Step("в базе данных '{dataBaseName}' выполнить sql-скрипт из файла '{sqlFileName}'")
    public void executeScriptFromFile(String dataBaseName, String sqlFileName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры sql-скрипта", dataTable);
        scenarioData.put(dataTable);
        executeScriptFromFile(dataBaseName, sqlFileName);
        TauLogger.console("Параметры sql-скрипта:", dataTable);
    }

    private void getNextResultSet(ResultSet resultSet) {
        try {
            resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getResultObject(ResultSet resultSet, String fieldName) {
        try {
            Object object = resultSet.getObject(fieldName);
            if (object instanceof Timestamp) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                return ((Timestamp) object).toLocalDateTime().format(df);
            } else {
                return resultSet.getString(fieldName);
            }
        } catch (SQLException ignored) {
            // ignored
        }
        return "null";
    }

    @Step("сохранить результат выполнения sql-скрипта в параметры:")
    public void saveSqlResultSet(DataTable dataTable) {
        List<List<String>> resultList = new ArrayList<>();
        resultList.add(Arrays.asList("Поле", "Значение", "Параметр"));
        run(() -> {
            ResultSet resultSet = scenarioData.get("sqlQueryResultSet");
            getNextResultSet(resultSet);
            dataTable.asMaps().forEach(map -> {
                String fieldName = TestDataUtils.get(map, "Поле");
                String parameterName = TestDataUtils.get(map, "Параметр");
                String fieldValue = getResultObject(resultSet, fieldName);
                scenarioData.put(parameterName, String.valueOf(fieldValue));
                resultList.add(Arrays.asList(fieldName, fieldValue, parameterName));
            });
        });
        DataTable resultDataTable = DataTable.create(resultList, TextUtils.tableConverter);
        TauLogger.console("Результаты выполнения sql-скрипта сохранены в параметры:", resultDataTable);
        TauLogger.attachDataTable("Результаты выполнения sql-скрипта сохранены в параметры", resultDataTable);
    }
}
