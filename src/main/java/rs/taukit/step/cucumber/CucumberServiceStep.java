package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;

import static rs.taukit.utils.BasicUtils.testContext;

public class CucumberServiceStep {

    @И("^служебный шаг: повторять действия до выполнения условий$")
    public void repeatActionsUntilSatisfyConditions() {
        testContext().startToSaveActions();
    }

    @И("^служебный шаг: повторять действия до выполнения условий \"(.+)\"$")
    public void repeatActionsUntilSatisfyConditions(String conditionsName) {
        testContext().startToSaveActions();
    }

}
