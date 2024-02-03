package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;

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

}
