package rs.taukit.step.definition;

import io.cucumber.java.Before;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.YamlParser;

import java.io.File;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class Hook {
    private static boolean testDataAttached = false;

    @Before
    public static void test() {
        if (testDataAttached) return;
        testDataAttached = true;
        File featureFile = new File(scenarioData().getFeaturePath());
        String featureDirPath = featureFile.getParent();
        String featureDir = featureDirPath.replaceAll("\\\\", "/");
        scenarioData().put("FEATURE_DIR", YamlParser.convert(featureDir));
        TauLogger.attachFile("Тестовые данные", YamlParser.write(scenarioData().getDataStorage().getAttachment()));
    }
}
