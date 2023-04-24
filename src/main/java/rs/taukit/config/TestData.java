package rs.taukit.config;

import io.cucumber.testng.Pickle;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.utils.YamlParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestData extends TestDataWork{
    private final String scenarioName;

    public TestData(Pickle pickle) {
        String featurePath = pickle.getUri().getPath();
        this.scenarioName = pickle.getName();
        this.data = getScenarioData(getFeatureData(featurePath));
    }

    private List<Map<String, ?>> getFeatureData(String featurePath){
        String scenariosPath = Config.getProperty("scenarios.path");
        String testDataPath = Config.getProperty("scenarios.data.path");
        String featureDataPath = featurePath
                .replace(scenariosPath, testDataPath)
                .replace("feature","yaml");
        try {
            return YamlParser.parse(featureDataPath);
        } catch (IOException e) {
            throw new ConfigFileNotFoundException(e, featureDataPath);
        }
    }
    private Map<String, ?> getScenarioData(List<Map<String,?>> featureData){
        return featureData.stream()
                .filter(data -> data.get("Сценарий").toString().equalsIgnoreCase(scenarioName))
                .findFirst().orElseThrow();
    }

    @Override
    public String toString() {
        return "Тестовые данные";
    }
}
