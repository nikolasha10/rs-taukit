package rs.taukit.utils;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

import java.io.File;

@SuppressWarnings("unchecked")
public class YamlParser {

    public static <T> T parse(String filePath) {
        ObjectMapper mapper = new YAMLMapper();
        File file = new File(filePath);
        return mapper.readValue(file, new TypeReference<>() {
        });
    }

    public static byte[] write(Object object, boolean splitLines) {
        YAMLMapper.Builder yamlBuilder = YAMLMapper.builder();
        if (!splitLines) {
            yamlBuilder.disable(YAMLWriteFeature.SPLIT_LINES);
        }
        ObjectMapper mapper = yamlBuilder.build();
//        mapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        return mapper.writeValueAsBytes(object);
    }

    public static <T> T convert(Object object) {
        return (T) ((object instanceof Integer) ? String.valueOf(object) : object);
    }
}
