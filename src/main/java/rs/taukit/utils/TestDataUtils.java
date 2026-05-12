package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.exceptions.PageFileNotFoundException;
import rs.taukit.testdata.TestData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TestDataUtils {
    public static <T> T get(Map<String, T> data, String fieldName) {
        if (data.containsKey(fieldName)) {
            return data.get(fieldName);
        }
        switch (fieldName) {
            case "empty":
                return YamlParser.convert("");
            case "space":
                return YamlParser.convert(" ");
            case "nbsp":
                return YamlParser.convert("\u00A0");
        }
        Supplier<RuntimeException> exceptionSupplier = () -> new DataTableFormatMismatchException(
                "Не найдено поле '%s'. Присутствуют поля: %s", fieldName, data.keySet());
        if (fieldName.contains(".") || fieldName.contains("->")) {
            String[] chunks = fieldName.split("->");
            if (chunks.length == 1) {
                chunks = fieldName.split("\\.");
            }
            T testData = null;
            for (int i = 0; i < chunks.length; i++) {
                String chunk = chunks[i].trim();
                if (i == 0) {
                    if (chunk.equals("System properties")) {
                        testData = YamlParser.convert(Config.getProperty(chunks[1].trim()));
                        i++;
                        continue;
                    }
                    testData = Optional.ofNullable(data.get(chunk)).orElseThrow(exceptionSupplier);
                } else {
                    if (testData instanceof TestData) {
                        TestData testDataObject = YamlParser.convert(testData);
                        testData = testDataObject.getOptional(chunk);
                    } else {
                        testData = null;
                    }
                    if (testData == null) {
                        throw exceptionSupplier.get();
                    }
                }
            }
            return testData;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public static <T> T getOptional(Map<String, T> data, String fieldName) {
        return data.getOrDefault(fieldName, null);
    }

    public static Map<String, ?> loadPageConfig(String pageName) {
        try {
            String pageFilePath = Config.getPageFilePath(pageName);
            return YamlParser.parse(pageFilePath);
        } catch (Exception e) {
            throw new PageFileNotFoundException(e, pageName);
        }
    }

    public static Map<String, String> transform(DataTable dataTable, String keyField, String valueField) {
        Map<String, String> dataMap = new HashMap<>();
        dataTable.asMaps().forEach(row -> dataMap.put(row.get(keyField), row.get(valueField)));
        return dataMap;
    }
}
