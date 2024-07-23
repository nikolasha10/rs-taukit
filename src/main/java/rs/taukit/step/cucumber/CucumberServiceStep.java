package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicServiceStep;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.scenarioStorage;

public class CucumberServiceStep {

    @И("^служебный шаг: повторять действия до выполнения условий$")
    public void repeatActionsUntilSatisfyConditions() {
        scenarioStorage().startToSaveActions();
    }

    @И("^служебный шаг: повторять действия до выполнения условий \"(.+)\"$")
    public void repeatActionsUntilSatisfyConditions(String conditionsName) {
        scenarioStorage().startToSaveActions();
    }

    @И("^служебный шаг: пропустить действия при выполнении условий$")
    public void skipActionsIfTrue(DataTable dataTable) {
        new AllureBasicServiceStep().skipActionsIfTrue(eval(dataTable));
    }

    @И("^служебный шаг: продолжить выполнять дальнейшие действия$")
    public void stopSkipActions() {
        new AllureBasicServiceStep().stopSkipActions();
    }

}
