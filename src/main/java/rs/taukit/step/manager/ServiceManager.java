package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import rs.taukit.testdata.ExpectedResult;

import java.util.Map;
import java.util.function.Predicate;

public class ServiceManager extends StepManager {
    public void skipActionsIfTrue(DataTable dataTable) {
        run(() -> {
            Predicate<Map<String, String>> predicate = condition -> new ExpectedResult(condition).checkResult().isConditionMet();
            if (dataTable.asMaps().stream().allMatch(predicate)) {
                scenarioData.put("SKIP_ACTIONS", true);
            }
        });
    }

    public void stopSkipActions() {
        Runnable runnable = () -> scenarioData.remove("SKIP_ACTIONS");
        runnable.run();
        scenarioStorage.saveStepAction(runnable);
    }
}
