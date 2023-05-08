package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.constant.TestDataField;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.YamlParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataStorage {
    private final Map<String, ?> dataStorage = new LinkedHashMap<>();

    public DataStorage(Map<String, ?> data) {
        data.forEach((fieldName, fieldValue) -> {
            if (fieldValue instanceof Map) {
                Map<String, ?> fieldData = YamlParser.convert(fieldValue);
                String fieldType = YamlParser.convert(fieldData.get("тип"));
                ITestData testData = TestDataField.getField(fieldType, fieldName, fieldValue);
                this.dataStorage.put(fieldName, YamlParser.convert(testData));
            } else if (fieldValue instanceof String) {
                String value = evalStrings(YamlParser.convert(fieldValue));
                this.dataStorage.put(fieldName, YamlParser.convert(value));
            } else if (fieldValue instanceof Integer) {
                this.dataStorage.put(fieldName, YamlParser.convert(fieldValue));
            }
        });

    }

    public <T> T get(String fieldName) {
        return YamlParser.convert(TestDataUtils.get(dataStorage, fieldName));
    }

    public void put(String key, Object value) {
        dataStorage.put(key, YamlParser.convert(value));
    }

    public LinkedHashMap<String, Object> getAttachments(String tableName) {
        Map<String, Object> linkTableMap = new LinkedHashMap<>();
        List<List<String>> dataTable = new ArrayList<>();
        dataStorage.forEach((fieldName, fieldValue) -> {
            if (fieldValue instanceof TestDataFile testDataFile) {
                dataTable.add(Arrays.asList(fieldName, testDataFile.getStringValue()));
                linkTableMap.put(testDataFile.getStringValue(), testDataFile);
            } else if (fieldValue instanceof TestDataDateTime testDataDateTime) {
                dataTable.add(Arrays.asList(fieldName, testDataDateTime.getStringValue()));
            } else if (fieldValue instanceof ITestData testData) {
                linkTableMap.putAll(testData.getAttachments());
                dataTable.add(Arrays.asList(fieldName, testData.getStringValue()));
            } else if (fieldValue instanceof String) {
                String value = evalStrings(YamlParser.convert(fieldValue));
                dataTable.add(Arrays.asList(fieldName, value));
            } else if (fieldValue instanceof Integer) {
                dataTable.add(Arrays.asList(fieldName, YamlParser.convert(fieldValue)));
            }
        });
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put(tableName, DataTable.create(dataTable, TextUtils.tableConverter));
        result.putAll(linkTableMap);
        return result;
    }

    public String evalStrings(String template) {
        String format = "\\$\\{(.+?)}";
        Matcher matcher = Pattern.compile(format).matcher(template);
        try {
            return matcher.replaceAll(matchResult -> {
                if (get(matchResult.group(1)) instanceof ITestData iTestData){
                    return iTestData.getStringValue();
                } else {
                    return get(matchResult.group(1));
                }
            });
        } catch (IllegalArgumentException e) {
            String message = String.format("Не удалось вычислить значение параметра '%s'", template);
            throw new TestDataException(e, message);
        }

    }

    public String evalObjects(String template) {
        String format = "\\$\\{(.+?)} -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(template);
        try {
            return matcher.replaceFirst(matchResult -> {
                TestData workData = get(matchResult.group(1));
                Object object = workData.getDataStorage().get(matchResult.group(2));
                if (object instanceof ITestData testData){
                    return testData.getStringValue();
                } else return object.toString();
            });
        } catch (Exception e) {
            String message = String.format("Не удалось вычислить значение параметра '%s'", template);
            throw new TestDataException(e, message);
        }
    }

}
