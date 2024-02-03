package rs.taukit.config;

import rs.taukit.utils.YamlParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dependencies {
    private final Map<String, List<Map<String, String>>> oneToMany;
    private final Map<String, List<Map<String, String>>> manyToOne;
    private final Map<String, String> statusMap = new HashMap<>();

    public Dependencies(String filePath) {
        Map<String, Object> dependencyMap = YamlParser.parse(filePath);
        this.oneToMany = YamlParser.convert(dependencyMap.get("Зависимость одного от многих"));
        this.manyToOne = YamlParser.convert(dependencyMap.get("Зависимость многих от одного"));
    }

    public Map<String, String> getScenarioDependencyMap(List<String> tagList) {
        List<String> tagValues = tagList.stream().map(tag -> tag.substring(1)).toList();
        Map<String, String> resultMap = new HashMap<>();
        oneToMany.entrySet().stream()
                .filter(entry -> tagValues.contains(entry.getKey()))
                .forEach(entry -> entry.getValue().forEach(resultMap::putAll));
        manyToOne.forEach((dependencyTag, dependentList) -> dependentList.stream()
                .filter(dependent -> dependent.keySet().stream().anyMatch(tagValues::contains))
                .forEach(dependent -> dependent.forEach((tag, status) -> resultMap.put(dependencyTag, status))));
        return resultMap;
    }

    public boolean checkStatus(String expected, String statusKey) {
        String current = statusMap.get(statusKey);
        return current == null || (!current.equals(expected) && !expected.equals("any"));
    }

    public void changeStatus(List<String> tagList, String status) {
        tagList.forEach(tag -> statusMap.put(tag.substring(1), status));
    }

}
