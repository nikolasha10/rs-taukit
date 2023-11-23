package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicDataBaseStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicDataBaseStep {
    private AllureBasicDataBaseStep dataBaseStep(String dataBaseName) {
        return new AllureBasicDataBaseStep(dataBaseName);
    }

    @И("^в базе данных \"(.+)\" выполнить sql-скрипт из файла \"(.+)\" с параметрами:$")
    public void runSqlScriptFromFile(String dataBaseName, String sqlFileName, DataTable dataTable) {
        dataBaseStep(dataBaseName).execute(sqlFileName, eval(dataTable));
    }

}
