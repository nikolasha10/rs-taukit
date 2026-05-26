package rs.taukit.config;

import rs.taukit.scenario.ScenarioManager;
import rs.taukit.utils.YamlParser;

import java.util.*;

public class Dependencies {
    private final Map<String, List<Map<String, String>>> oneToMany;
    private final Map<String, List<Map<String, String>>> manyToOne;
    private final Map<String, String> statusMap = new HashMap<>();
    private final Map<String, Set<String>> checkpointMap = new HashMap<>();

    public Dependencies(String filePath) {
        Map<String, Object> dependencyMap = YamlParser.parse(filePath);
        Map<String, List<Map<String, String>>> oneToMany = YamlParser.convert(dependencyMap.get("Зависимость одного от многих"));
        this.oneToMany = Optional.ofNullable(oneToMany).orElse(new HashMap<>());
        Map<String, List<Map<String, String>>> manyToOne = YamlParser.convert(dependencyMap.get("Зависимость многих от одного"));
        this.manyToOne = Optional.ofNullable(manyToOne).orElse(new HashMap<>());
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

    /**
     * @param expected  ожидаемый статус сценария, от которого зависит проверяемый
     * @param statusKey - тег этого сценария. По нему будет получен статус
     * @return true - если зависимость не выполнена, false - если выполнена или ждать её выполнения не требуется,
     * например, потому что этот сценарий не был включен в текущий testRun и статус зависимости = any
     */

    public boolean checkStatus(String expected, String statusKey) {
        String current = statusMap.get(statusKey);
        boolean anyStatusExpected = expected.equals("any");
        boolean isDependencyInTestRun = ScenarioManager.getInstance().checkDependency("@" + statusKey);
        boolean isCheckpointPassed = checkpointMap.getOrDefault(statusKey, new HashSet<>()).contains(expected);
        return Config.getBooleanProperty("trackDependencies")
                && (current == null && isDependencyInTestRun
                || !(current == null || current.equals(expected) || anyStatusExpected || isCheckpointPassed));
    }

    public void changeStatus(List<String> tagList, String status) {
        tagList.forEach(tag -> statusMap.put(tag.substring(1), status));
    }

    public void setCheckpointPassed(List<String> tagList, String checkpoint) {
        tagList.forEach(tag -> {
            String key = tag.substring(1);
            checkpointMap.computeIfAbsent(key, k -> new HashSet<>()).add(checkpoint);
        });
    }
}
