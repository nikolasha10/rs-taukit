package step.scenario;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import step.Story;
import step.implementer.Menu;

import static rs.taukit.step.utils.BasicUtils.eval;

public class Scenario {

    @И("^войти в \"(ЕФР)\" под учетными данными пользователя \"(.+)\"$")
    public void loginIntoApplication(String appName, String employeeDescription) {
        Story.loginIntoApplication(appName, employeeDescription);
    }
    @И("^выполнить сценарий \"(.+)\"$")
    public void runScenario(String scenarioName) {
        Story.runScenario(scenarioName);
    }
    @И("^выполнить сценарий \"(.+)\" с параметрами:$")
    public void runScenario(String scenarioName, DataTable dataTable) {
        Story.runScenario(scenarioName, eval(dataTable));
    }
    @И("^через \"(.+)\" перейти на страницу \"(.+)\"$")
    public void selectMenuItem(String menuName, String itemName) {
        Menu menu = new Menu(menuName);
        menu.selectMenuItem(itemName);
    }
}
