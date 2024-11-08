package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.BasicUtils;

import java.util.Map;
import java.util.function.Predicate;

public class AllureBasicServiceStep extends BasicUtils {
    public void skipActionsIfTrue(DataTable dataTable) {
        run(() -> {
            Predicate<Map<String, String>> predicate = condition -> new ExpectedResult(condition).checkResult().isConditionMet();
            if (dataTable.asMaps().stream().allMatch(predicate)) {
                scenarioData().put("SKIP_ACTIONS", true);
            }
        });
    }

    public void stopSkipActions() {
        Runnable runnable = () -> scenarioData().remove("SKIP_ACTIONS");
        scenarioStorage().saveStepAction(runnable);
        runnable.run();
    }
}
