package rs.taukit.element;

import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.util.*;
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

    public List<String> get(List<String> fields) {
        String optionName = options.keySet().stream().filter(fields::contains).findFirst().orElse(null);
        return optionName == null ? null : Arrays.asList(optionName, options.get(optionName).toString());
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

    public <T> void put(String option, T value) {
        options.put(option, value);
    }

    public ElementModel replace(ElementModel model) {
        Map<String, Object> result = new HashMap<>(model.options);
        options.forEach((name, option) -> result.put(name, replace(option, model.options.get(name))));
        return new ElementModel(result);
    }

    private Object replace(Object templateOption, Object testdataOption) {
        if (testdataOption == null) return templateOption;
        if (!(testdataOption instanceof Map)) {
            return testdataOption;
        }
        Map<String, Object> testDataOptionStructure = YamlParser.convert(testdataOption);
        Map<String, Object> templateOptionStructure = YamlParser.convert(templateOption);
        Map<String, Object> result = new HashMap<>(testDataOptionStructure);
        templateOptionStructure.forEach((name, option) -> result.put(name, replace(option, testDataOptionStructure.get(name))));
        return result;
    }

    public String getType() {
        return options.containsKey("тип") ? get("тип") : "";
    }

    public String getSearchMethod() {
        return options.keySet().stream().filter("xpath,test-id"::contains).findFirst().orElse("неизвестный метод поиска");
    }

    @Override
    public String toString() {
        return options.toString();
    }
}
