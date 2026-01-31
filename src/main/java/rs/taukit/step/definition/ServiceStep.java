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

    @И("^сформировать отчёт$")
    public void createReport() {
        super.createReport();
    }

    @И("^служебный шаг: кликнуть ЛКМ в области над элементом \"(.+)\"$")
    public void forceClickElement(String elementName) {
        super.forceClickElement(eval(elementName));
    }

    @И("^служебный шаг: создать автотест в TestIT$")
    public void createTestItAutotest() {
        super.createTestItAutotest();
    }

    @И("^служебный шаг: обновить автотест в TestIT$")
    public void updateTestItAutotest() {
        super.updateTestItAutotest();
    }

    @И("^служебный шаг: установить запрет на одновременное выполнение по ключу \"(.+)\"$")
    public void setMutex(String mutexName) {
        super.setMutex(mutexName);
    }

    @И("^служебный шаг: снять запрет на одновременное выполнение по ключу \"(.+)\"$")
    public void releaseMutex(String mutexName) {
        super.releaseMutex(mutexName);
    }

    @И("^служебный шаг: ошибка на следующем шаге относится к категориям:$")
    public void releaseMutex(DataTable dataTable) {
        super.addErrorCategories(dataTable);
    }
}
