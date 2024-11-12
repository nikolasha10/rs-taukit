package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.exceptions.PageFileNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestDataUtils {
    public static <T> T get(Map<String, T> data, String fieldName) {
        return Optional.ofNullable(data.get(fieldName))
                .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле '%s'. Присутствуют поля: %s",
                        fieldName, data.keySet()));
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
