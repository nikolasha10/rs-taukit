package rs.taukit.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import rs.taukit.exceptions.ConfigFileNotFoundException;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class YamlParser {

    public static <T> T parse(String filePath) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File file = new File(filePath);
        try {
            return mapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new ConfigFileNotFoundException(e, filePath);
        }
    }

    public static byte[] write(Object object) {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.SPLIT_LINES);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        try {
            return mapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convert(Object object) {
        return (T) ((object instanceof Integer) ? String.valueOf(object) : object);
    }
}
