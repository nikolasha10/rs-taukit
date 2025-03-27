package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
public class ScenarioData {
    private final DataStorage dataStorage;
    private final String scenarioName;
    private final String featurePath;
    private final Map<String, DataStorage> nameSpaces;

    public ScenarioData(Pickle pickle) {
        this.featurePath = pickle.getUri().getPath();
        this.scenarioName = pickle.getName();
        this.dataStorage = new DataStorage(featurePath, getScenarioData(getFeatureData(featurePath)));
        this.nameSpaces = new HashMap<>();
        put("Адрес стенда", Config.getSTAND().getUrl());
        List<String> tagList = pickle.getTags().stream()
                .filter(tag -> !tag.matches("@(.+)\\(\"(.+)\",\"(.+)\"\\)"))
                .toList();
        put("Теги", String.join(" ", tagList));
    }

    private File getFeatureDataFile(String featurePath) {
        String featureDataPath = featurePath.replace("feature", "yaml");
        return new File(featureDataPath);
    }

    private List<Map<String, ?>> getFeatureData(String featurePath) {
        File featureDataFile = getFeatureDataFile(featurePath);
        return featureDataFile.exists() ? YamlParser.parse(featureDataFile.getAbsolutePath()) : new ArrayList<>();
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

    public void put(String nameSpace, String fieldName, Object value) {
        nameSpaces.putIfAbsent(nameSpace, new DataStorage(new HashMap<>()));
        nameSpaces.get(nameSpace).put(fieldName, value);
    }

    public void put(DataTable dataTable) {
        dataTable.asMaps().forEach(item -> {
            String parameterName = TestDataUtils.get(item, "Параметр");
            String parameterValue = TestDataUtils.getOptional(item, "Значение");
            put(parameterName, Optional.ofNullable(parameterValue).orElse(""));
        });
    }

    public void put(DataTable dataTable, String nameSpace) {
        nameSpaces.putIfAbsent(nameSpace, new DataStorage(new HashMap<>()));
        dataTable.asMaps().forEach(item -> {
            String parameterName = TestDataUtils.get(item, "Параметр");
            String parameterValue = TestDataUtils.get(item, "Значение");
            nameSpaces.get(nameSpace).put(parameterName, parameterValue);
        });
    }

    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    public <T> T get(String nameSpace, String fieldName) {
        return nameSpaces.get(nameSpace).get(fieldName);
    }

    public void remove(String key) {
        dataStorage.remove(key);
    }

    public <T> T getOptional(String fieldName) {
        return dataStorage.getOptional(fieldName);
    }

    public <T> T getOptional(String nameSpace, String fieldName) {
        return nameSpaces.get(nameSpace).getOptional(fieldName);
    }

    public String evalObjectField(String template) {
        return dataStorage.evalObjectField(template);
    }

    public <T> T evalObject(String template) {
        return dataStorage.evalObject(template);
    }

    public String evalString(String template) {
        return dataStorage.evalString(template);
    }

    public void editTestDataFile(Map<String, ?> replacements) {
        String testDataFilePath = getFeatureDataFile(featurePath).getAbsolutePath();
        String newLines = new String(YamlParser.write(replacements, false), StandardCharsets.UTF_8)
                .replaceAll("---\n", "").trim();
        if (FileUtils.readFile(testDataFilePath).contains(newLines)) return;
        List<String> lines = FileUtils.readFileToLineList(testDataFilePath);
        int index = lines.indexOf("- Сценарий: " + scenarioName);
        lines.add(index + 1, "  " + newLines);
        FileUtils.writeFile(testDataFilePath, String.join("\n", lines));
    }
}
