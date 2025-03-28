package rs.taukit.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import rs.taukit.exceptions.AutoTestBrokenException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonParser {

    public static <T> T parse(String jsonText) {
        ObjectMapper jsonMapper = new JsonMapper();
        List<?> resultList = new ArrayList<>();
        Map<String, ?> resultMap = new HashMap<>();
        try {
            resultList = YamlParser.convert(jsonMapper.readValue(jsonText, resultList.getClass()));
            return (T) resultList;
        } catch (JsonProcessingException ignored) {
            try {
                resultMap = YamlParser.convert(jsonMapper.readValue(jsonText, resultMap.getClass()));
                return (T) resultMap;
            } catch (JsonProcessingException e) {
                throw new AutoTestBrokenException(e, "Ошибка при разборе json");
            }
        }
    }
}
