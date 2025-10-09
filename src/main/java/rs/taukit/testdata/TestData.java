package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.HasText;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class TestData implements ITestData, HasText {
    private DataStorage dataStorage;
    private String fieldName;
    private String searchValue;
    private String value;

    public TestData(String value) {
        this.value = Optional.ofNullable(value).orElse("");
    }

    public TestData(String fieldName, LinkedHashMap<String, ?> parameters) {
        this(fieldName, parameters, "");
    }

    public TestData(String fieldName, LinkedHashMap<String, ?> parameters, String dirPath) {
        this.fieldName = fieldName;
        String filePath = YamlParser.convert(parameters.get("файл"));
        if (filePath == null) {
            searchValue = fieldName;
            dataStorage = new DataStorage(parameters);
            return;
        }
        filePath = Config.eval(filePath);
        Map<String, String> searchBy = YamlParser.convert(parameters.get("поиск по полям"));
        this.searchValue = searchBy.values().stream().toList().getFirst();
        File file = new File(filePath);
        if (!file.exists()) {
            filePath = dirPath + File.separator + filePath;
        }
        List<Map<String, ?>> dataList = YamlParser.parse(filePath);
        String filePathTmp = filePath;
        Predicate<Map<String, ?>> condition = item -> searchBy.entrySet().stream().allMatch(entry -> {
            String dataFieldName = entry.getKey();
            String expectedValue = entry.getValue();
            Object dataFieldValue = item.get(dataFieldName);
            if (dataFieldValue instanceof List<?> listOfValues) {
                List<String> values = YamlParser.convert(listOfValues);
                return values.contains(expectedValue);
            } else {
                return dataFieldValue != null && dataFieldValue.toString().equalsIgnoreCase(expectedValue);
            }
        });
        Supplier<RuntimeException> exception = () -> new TestDataException("Не удалось получить данные для поля '%s'. %s",
                fieldName, filePathTmp);
        try {
            dataStorage = new DataStorage(filePath, dataList.stream().filter(condition).findFirst().orElseThrow(exception));
        } catch (ClassCastException e) {
            throw new TestDataException(e, "Неверный формат данных для поля '%s' в файле '%s'",fieldName, filePathTmp);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getAttachments() {
        return dataStorage.getAttachments(searchValue);
    }

    @Override
    public LinkedHashMap<String, Object> getAttachment() {
        return dataStorage.getAttachment();
    }

    @Override
    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    @Override
    public <T> T getOptional(String fieldName) {
        return dataStorage.getOptional(fieldName);
    }

    @Override
    public String getStringValue() {
        return searchValue;
    }

    @Override
    public String getText() {
        return getValue();
    }

    @Override
    public boolean hasText(String expectedValue) {
        return getText().equals(expectedValue);
    }

    @Override
    public Boolean containsText(String expectedText) {
        return getText().contains(expectedText);
    }

    @Override
    public Boolean matches(String format) {
        Matcher matcher = Pattern.compile(format).matcher(getText());
        return matcher.matches();
    }

}
