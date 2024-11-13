package rs.taukit.step.definition;

import io.cucumber.java.Before;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.YamlParser;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class Hook {
    private static boolean testDataAttached = false;

    @Before
    public static void test() {
        if (testDataAttached) return;
        testDataAttached = true;
        TauLogger.attachFile("Тестовые данные", YamlParser.write(scenarioData().getDataStorage().getAttachment()));
    }
}
