package rs.taukit.element;

import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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

    public <T> T getOrDefault(String option, T defaultValue) {
        return YamlParser.convert(Optional.ofNullable(TestDataUtils.getOptional(options, option)).orElse(defaultValue));
    }

    public <T> T get(String option, boolean required) {
        try {
            return YamlParser.convert(TestDataUtils.get(options, option));
        } catch (DataTableFormatMismatchException e) {
            if (required) throw e;
            return null;
        }
    }

    public String getType() {
        return options.containsKey("тип") ? get("тип") : "";
    }

    @Override
    public String toString() {
        return options.toString();
    }
}
