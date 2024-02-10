package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ScenarioData {
    private final DataStorage dataStorage;
    private final String scenarioName;
    private final String featurePath;

    public ScenarioData(Pickle pickle) {
        this.featurePath = pickle.getUri().getPath();
        this.scenarioName = pickle.getName();
        this.dataStorage = new DataStorage(featurePath, getScenarioData(getFeatureData(featurePath)));
        put("Адрес стенда", Config.getSTAND().getUrl());
    }

    private List<Map<String, ?>> getFeatureData(String featurePath) {
        String featureDataPath = featurePath.replace("feature", "yaml");
        File featureDataFile = new File(featureDataPath);
        return featureDataFile.exists() ? YamlParser.parse(featureDataPath) : new ArrayList<>();
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

    public <T> T getOptional(String fieldName) {
        return dataStorage.getOptional(fieldName);
    }

    public String evalObjectField(String template) {
        return dataStorage.evalObjectField(template);
    }

    public <T> T evalObject(String template) {
        return dataStorage.evalObject(template);
    }

    public String evalStrings(String template) {
        return dataStorage.evalStrings(template);
    }
}
