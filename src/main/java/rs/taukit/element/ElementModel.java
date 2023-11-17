package rs.taukit.element;

import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ElementModel {
    private final LinkedHashMap<String, Object> options;

    public ElementModel(Map<String, Object> optionsMap) {
        this.options = new LinkedHashMap<>(optionsMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue)));
    }

    public <T> T get(String option) {
        return YamlParser.convert(TestDataUtils.get(options, option));
    }

    @Override
    public String toString() {
        return options.toString();
    }
}
