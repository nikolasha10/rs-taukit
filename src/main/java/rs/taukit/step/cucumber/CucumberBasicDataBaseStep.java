package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicDataBaseStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicDataBaseStep {
    private AllureBasicDataBaseStep dataBaseStep(String dataBaseName) {
        return new AllureBasicDataBaseStep(dataBaseName);
    }
    private AllureBasicDataBaseStep dataBaseStep() {
        return new AllureBasicDataBaseStep();
    }

    @И("^в базе данных \"(.+)\" выполнить sql-скрипт из файла \"(.+)\"$")
    public void runSqlScriptFromFile(String dataBaseName, String sqlFileName) {
        dataBaseStep(dataBaseName).execute(sqlFileName);
    }
    @И("^в базе данных \"(.+)\" выполнить sql-скрипт из файла \"(.+)\" с параметрами:$")
    public void runSqlScriptFromFile(String dataBaseName, String sqlFileName, DataTable dataTable) {
        dataBaseStep(dataBaseName).execute(sqlFileName, eval(dataTable));
    }

    @И("^в базе данных \"(.+)\" для всех значений списка \"(.+)\" выполнить sql-скрипт из файла \"(.+)\" с параметрами:$")
    public void runSqlScriptFromFileForList(String dataBaseName, String listOfValues, String sqlFileName, DataTable dataTable) {
        dataBaseStep(dataBaseName).execute(sqlFileName, eval(dataTable));
    }

    @И("^сохранить результат выполнения sql-скрипта в параметры:$")
    public void saveSqlResultSet(DataTable dataTable) {
        dataBaseStep().saveSqlResultSet(eval(dataTable));
    }

}
