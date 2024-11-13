package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.ServiceManager;

import static rs.taukit.utils.BasicUtils.eval;

public class ServiceStep extends ServiceManager {

    @И("^служебный шаг: повторять действия до выполнения условий$")
    public void repeatActionsUntilSatisfyConditions() {
        scenarioStorage.startToSaveActions();
    }

    @И("^служебный шаг: повторять действия до выполнения условий \"(.+)\"$")
    public void repeatActionsUntilSatisfyConditions(String conditionsName) {
        scenarioStorage.startToSaveActions();
    }

    /**
     * Позволяет задать условия для пропуска шагов ниже, до служебного шага "продолжить выполнять дальнейшие действия"
     * Пропускать можно только шаги вида "* выполнить сценарий ..."
     *
     * @param dataTable - datatable
     */
    @И("^служебный шаг: пропустить действия при выполнении условий$")
    public void skipActionsIfTrue(DataTable dataTable) {
        super.skipActionsIfTrue(eval(dataTable));
    }

    @И("^служебный шаг: продолжить выполнять дальнейшие действия$")
    public void stopSkipActions() {
        super.stopSkipActions();
    }

}
