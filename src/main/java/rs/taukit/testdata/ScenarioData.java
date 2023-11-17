package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ScenarioData {
    private final DataStorage dataStorage;
    private final String scenarioName;

    public ScenarioData(Pickle pickle) {
        String featurePath = pickle.getUri().getPath();
        this.scenarioName = pickle.getName();
        this.dataStorage = new DataStorage(getScenarioData(getFeatureData(featurePath)));
    }

    private List<Map<String, ?>> getFeatureData(String featurePath) {
        String scenariosPath = Config.getProperty("scenarios.path");
//        String testDataPath = Config.getProperty("scenarios.data.path");
        String featureDataPath = featurePath
//                .replace(scenariosPath, testDataPath)
                .replace("feature", "yaml");
        return YamlParser.parse(featureDataPath);
    }

    private Map<String, ?> getScenarioData(List<Map<String, ?>> featureData) {
        return featureData.stream()
                .filter(data -> data.get("Сценарий").toString().equalsIgnoreCase(scenarioName))
                .findFirst().orElse(new LinkedHashMap<>());
    }

    public LinkedHashMap<String, Object> getAttachments() {
        return dataStorage.getAttachments("Тестовые параметры");
    }

    @Override
    public String toString() {
        return "Тестовые данные";
    }

    public void put(String fieldName, Object value) {
        dataStorage.put(fieldName, value);
    }

    public void put(DataTable dataTable) {
        dataTable.asMaps().forEach(item -> {
            String parameterName = TestDataUtils.get(item, "Параметр");
            String parameterValue = TestDataUtils.get(item, "Значение");
            put(parameterName, parameterValue);
        });
    }

    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    public String evalObjects(String template) {
        return dataStorage.evalObjects(template);
    }

    public String evalStrings(String template) {
        return dataStorage.evalStrings(template);
    }
}
