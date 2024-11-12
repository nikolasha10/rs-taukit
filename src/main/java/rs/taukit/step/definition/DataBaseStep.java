package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.DBManager;

import static rs.taukit.utils.BasicUtils.eval;

public class DataBaseStep extends DBManager {
    @И("^в базе данных \"(.+)\" выполнить sql-скрипт из файла \"(.+)\"$")
    public void runSqlScriptFromFile(String dataBaseName, String sqlFileName) {
        super.executeScriptFromFile(dataBaseName, sqlFileName);
    }

    @И("^в базе данных \"(.+)\" выполнить sql-скрипт из файла \"(.+)\" с параметрами:$")
    public void runSqlScriptFromFile(String dataBaseName, String sqlFileName, DataTable dataTable) {
        super.executeScriptFromFile(dataBaseName, sqlFileName, eval(dataTable));
    }

    @И("^сохранить результат выполнения sql-скрипта в параметры:$")
    public void saveSqlResultSet(DataTable dataTable) {
        super.saveSqlResultSet(eval(dataTable));
    }

}
