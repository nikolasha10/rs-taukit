package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
public class TestData implements ITestData {
    private DataStorage dataStorage;
    private String fieldName;
    private String searchValue;

    public TestData() {
    }

    public TestData(String fieldName, LinkedHashMap<String, ?> parameters) {
        this(fieldName, parameters, "");
    }

    public TestData(String fieldName, LinkedHashMap<String, ?> parameters, String dirPath) {
        this.fieldName = fieldName;
        String filePath = YamlParser.convert(parameters.get("файл"));
        Map<String, String> searchBy = YamlParser.convert(parameters.get("поиск по полям"));
        this.searchValue = searchBy.values().stream().toList().get(0);
        File file = new File(filePath);
        if (!file.exists()) {
            filePath = dirPath + File.separator + filePath;
        }
        List<Map<String, ?>> dataList = YamlParser.parse(filePath);
        Predicate<Map<String, ?>> condition = item -> searchBy.entrySet().stream().allMatch(entry -> {
            String dataFieldName = entry.getKey();
            String expectedValue = entry.getValue();
            Object dataFieldValue = item.get(dataFieldName);
            if (dataFieldValue instanceof List<?> listOfValues) {
                List<String> values = YamlParser.convert(listOfValues);
                return values.contains(expectedValue);
            } else {
                return dataFieldValue.toString().equalsIgnoreCase(expectedValue);
            }
        });
        Supplier<RuntimeException> exception = () -> new TestDataException("Не удалось получить данные для поля '%s'", fieldName);
        dataStorage = new DataStorage(filePath, dataList.stream().filter(condition).findFirst().orElseThrow(exception));

    }

    @Override
    public LinkedHashMap<String, Object> getAttachments() {
        return dataStorage.getAttachments(searchValue);
    }

    @Override
    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    @Override
    public String getStringValue() {
        return searchValue;
    }
}
