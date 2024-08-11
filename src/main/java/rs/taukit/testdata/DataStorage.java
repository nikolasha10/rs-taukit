package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.constant.TestDataField;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataStorage {
    private final Map<String, ?> dataStorage = new LinkedHashMap<>();

    public DataStorage(String featureFilePath, Map<String, ?> data) {
        File featureFile = new File(featureFilePath);
        String featureDirPath = featureFile.getParent();
        if (featureDirPath != null) {
            String featureDir = featureDirPath.replaceAll("\\\\", "/");
            dataStorage.putIfAbsent("FEATURE_DIR", YamlParser.convert(featureDir));
        }

        data.forEach((fieldName, fieldValue) -> {
            if (fieldValue instanceof Map) {
                Map<String, ?> fieldData = YamlParser.convert(fieldValue);
                String fieldType = YamlParser.convert(fieldData.get("тип"));
                ITestData testData = fieldType == null
                        ? new TestData(fieldName, new LinkedHashMap<>(fieldData), featureDirPath)
                        : TestDataField.getField(fieldType, fieldName, fieldValue, featureDirPath);
                this.dataStorage.put(fieldName, YamlParser.convert(testData));
            } else if (fieldValue instanceof List) {
                this.dataStorage.put(fieldName, YamlParser.convert(fieldValue));
            } else if (fieldValue instanceof String) {
                String value = evalString(Config.eval(YamlParser.convert(fieldValue)));
                this.dataStorage.put(fieldName, YamlParser.convert(value));
            } else if (fieldValue instanceof Integer || fieldValue instanceof Boolean) {
                this.dataStorage.put(fieldName, YamlParser.convert(fieldValue));
            }
        });
    }

    public DataStorage(Map<String, ?> data) {
        this("", data);
    }

    public <T> T get(String fieldName) {
        return YamlParser.convert(TestDataUtils.get(dataStorage, fieldName));
    }

    public <T> T getOptional(String fieldName) {
        return YamlParser.convert(TestDataUtils.getOptional(dataStorage, fieldName));
    }

    public void put(String key, Object value) {
        dataStorage.put(key, YamlParser.convert(value));
    }

    public void remove(String key) {
        dataStorage.remove(key);
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
                String value = evalString(YamlParser.convert(fieldValue));
                dataTable.add(Arrays.asList(fieldName, value));
            } else if (fieldValue instanceof Integer) {
                dataTable.add(Arrays.asList(fieldName, YamlParser.convert(fieldValue)));
            } else {
                dataTable.add(Arrays.asList(fieldName, fieldValue.toString()));
            }
        });
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put(tableName, DataTable.create(dataTable, TextUtils.tableConverter));
        result.putAll(linkTableMap);
        return result;
    }

    public String evalString(String template) {
        String format = "\\$\\{(.+?)}";
        Matcher matcher = Pattern.compile(format).matcher(template);
        try {
            if (matcher.matches()) {
                Object optional = getOptional(matcher.group(1));
                if (optional != null && optional.toString().startsWith("$")) {
                    return optional.toString();
                }
            }
            return matcher.replaceAll(matchResult -> {
                if (get(matchResult.group(1)) instanceof ITestData iTestData) {
                    return iTestData.getStringValue();
                } else {
                    return get(matchResult.group(1)).toString();
                }
            });
        } catch (IllegalArgumentException e) {
            String message = String.format("Не удалось вычислить значение параметра \"%s\"", template);
            throw new TestDataException(e, message);
        }
    }

    public String evalObjectField(String template) {
        String format = "\\$\\{(.+?)} -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(template);
        try {
            return matcher.replaceFirst(matchResult -> {
                TestData workData = get(matchResult.group(1));
                Object object = workData.getDataStorage().getOptional(matchResult.group(2));
                if (object == null) return "null";
                if (object instanceof ITestData testData) {
                    return testData.getStringValue();
                } else return object.toString();
            });
        } catch (Exception e) {
            String message = String.format("Не удалось вычислить значение параметра \"%s\"", template);
            throw new TestDataException(e, message);
        }
    }

    public <T> T evalObject(String template) {
        String format = "\\$\\{(.+?)} -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(template);
        try {
            if (!matcher.matches()) {
                throw new TestDataException("Ключ \"%s\" не соответствует формату \"%s\"", template, format);
            }
            TestData workData = get(matcher.group(1));
            return workData.getDataStorage().get(matcher.group(2));
        } catch (Exception e) {
            String message = String.format("Не удалось вычислить значение параметра \"%s\"", template);
            throw new TestDataException(e, message);
        }
    }
}
