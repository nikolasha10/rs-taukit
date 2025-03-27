package rs.taukit.step.definition;

import io.cucumber.java.Before;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.scenario;
import static rs.taukit.utils.BasicUtils.scenarioData;

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
}
