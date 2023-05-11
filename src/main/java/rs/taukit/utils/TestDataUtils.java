package rs.taukit.utils;

import rs.taukit.config.Config;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.exceptions.PageFileNotFoundException;

import java.util.Map;
import java.util.Optional;

public class TestDataUtils {
    public static <T> T get(Map<String,T> data, String fieldName){
        return Optional.ofNullable(data.get(fieldName))
                .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле '%s'. Присутствуют поля: %s",
                        fieldName, data.keySet()));
    }

    public static Map<String, ?> loadPageConfig(String pageName) {
        try {
            String pagesDirPath = Config.getProperty("pagesDirPath");
            String pageMappingFilePath = pagesDirPath + "/pageMapping.yaml";
            Map<String, String> pageMapping = YamlParser.parse(pageMappingFilePath);
            String pageFilePath = pagesDirPath + "/" + pageMapping.get(pageName);
            return YamlParser.parse(pageFilePath);
        } catch (Exception e) {
            throw new PageFileNotFoundException(e, pageName);
        }
    }

}
