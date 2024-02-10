package step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import step.allure.AllureEfrStep;
import step.allure.Menu;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberEfrStep {

    @И("^войти в \"(ЕФР)\" под учетными данными пользователя \"(.+)\"$")
    public void loginIntoApplication(String appName, String employeeDescription) {
        AllureEfrStep.loginIntoApplication(appName, employeeDescription);
    }

    @И("^выполнить сценарий \"(.+)\"$")
    public void runScenario(String scenarioName) {
        AllureEfrStep.runScenarioWrap(scenarioName);
    }

    @И("^выполнить сценарий \"(.+)\" с параметрами:$")
    public void runScenario(String scenarioName, DataTable dataTable) {
        AllureEfrStep.runScenarioWrap(scenarioName, eval(dataTable));
    }

    @И("^выполнять сценарий \"(.+)\" до выполнения условий:$")
    public void runScenarioUntil(String scenarioName, DataTable dataTable) {
        AllureEfrStep.runScenarioUntilWrap(scenarioName, eval(dataTable));
    }

    @И("^через \"(.+)\" перейти на страницу \"(.+)\"$")
    public void selectMenuItem(String menuName, String itemName) {
        Menu menu = new Menu(menuName);
        menu.selectMenuItem(itemName);
    }

    @И("^сформировать новую заявку по операции \"(.+)\"$")
    public void createNewApplication(String operationName){
        AllureEfrStep.createNewApplication(operationName);
    }
}
