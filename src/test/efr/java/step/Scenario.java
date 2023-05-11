package step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.utils.BasicUtils;
import step.utils.Utils;

import static rs.taukit.step.utils.BasicUtils.eval;

public class Scenario {

    @И("^войти в \"(ЕФР)\"$")
    public void loginIntoApplication(String appName) {
        Story.loginIntoApplication(appName);
    }

    @И("^выполнить сценарий \"(.+)\"$")
    public void runScenario(String scenarioName) {
        Story.runScenario(scenarioName);
    }
    @И("^выполнить сценарий \"(.+)\" с параметрами:$")
    public void runScenario(String scenarioName, DataTable dataTable) {
        Story.runScenario(scenarioName, eval(dataTable));
    }

}
