package rs.taukit.step.definition;

import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.Step;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.*;

public class Hook {
    private static final Map<String, Boolean> testDataAttached = new HashMap<>();

    @Before
    public static void test() {
        String key = scenario().getFunctionality() + scenario().getName();
        if (testDataAttached.containsKey(key)) return;
        testDataAttached.put(key, true);
        File featureFile = new File(scenarioData().getFeaturePath());
        String featureDirPath = featureFile.getParent();
        String featureDir = featureDirPath.replaceAll("\\\\", "/");
        scenarioData().put("FEATURE_DIR", YamlParser.convert(featureDir));
        TauLogger.attachYaml("Тестовые данные", YamlParser.write(scenarioData().getDataStorage().getAttachment(), true));
    }

    @BeforeStep
    public void saveStepPath(Scenario scenario, Step step) {
        List<String> stepPath = scenarioData().getOptional("STEP_PATH");
        if (stepPath == null) {
            stepPath = new ArrayList<>();
        }
        String scenarioName = scenario.getName();
        if (stepPath.isEmpty() || !stepPath.getLast().equals(scenarioName)) {
            if (!scenarioName.equals(scenario().getName())) {
                stepPath.add(scenario.getName());
            }
            scenarioData().put("STEP_PATH", stepPath);
        }
        scenarioData().put("CURRENT_STEP_NAME", eval(step.getText()));
    }

}
