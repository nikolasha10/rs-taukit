package rs.taukit.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import rs.taukit.exceptions.AutoTestBrokenException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
        } catch (JacksonException ignored) {
            try {
                resultMap = YamlParser.convert(jsonMapper.readValue(jsonText, resultMap.getClass()));
                return (T) resultMap;
            } catch (Exception e) {
                throw new AutoTestBrokenException(e, "Ошибка при разборе json");
            }
        }
    }

    public static byte[] write(Object object) {
        ObjectMapper jsonMapper = new JsonMapper();
        return jsonMapper.writeValueAsBytes(object);
    }

    public static String writeAsString(Object object) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .defaultPropertyInclusion(JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.NON_NULL
                )).build();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
