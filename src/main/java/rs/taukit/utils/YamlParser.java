package rs.taukit.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class YamlParser {
    public static <T> T parse(String filePath) throws IOException {
        File file = new File(filePath);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(file, new TypeReference<>() {});
    }

    public static <T> T convert(Object object){
        return (T) ((object instanceof Integer) ? String.valueOf(object) : object);
    }
}
