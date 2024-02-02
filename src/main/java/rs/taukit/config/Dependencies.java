package rs.taukit.config;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import rs.taukit.records.DependencyItem;
import rs.taukit.records.DependencyStatus;
import rs.taukit.records.FunctionalityDependency;
import rs.taukit.records.ScenarioDependency;
import rs.taukit.utils.YamlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dependencies {
    private final List<FunctionalityDependency> scenarioDependencies;
    private final List<List<Object>> waitingScenarios = new ArrayList<>();

    public Dependencies(String filePath) {
        Map<String, List<Object>> dependencies = YamlParser.parse(filePath);
        scenarioDependencies = dependencies.get("Зависимость сценария").stream()
                .map(YamlParser::<Map<String, List<Object>>>convert)
                .map(functionalityGroup -> {
                    List<ScenarioDependency> scenarioDependencyList = functionalityGroup.get("Сценарии").stream()
                            .map(YamlParser::<Map<String, List<Object>>>convert)
                            .map(scenarioGroup -> {
                                List<DependencyItem> items = scenarioGroup.get("Зависит от").stream()
                                        .map(YamlParser::<Map<String, List<Object>>>convert)
                                        .map(dependency -> {
                                            Map<String, String> scenarioStatuses = YamlParser.convert(dependency.get("Сценарии"));
                                            return new DependencyItem(
                                                    YamlParser.convert(dependency.get("Функционал")),
                                                    scenarioStatuses.entrySet().stream()
                                                            .map(entry -> new DependencyStatus(entry.getKey(), entry.getValue()))
                                                            .collect(Collectors.toMap(
                                                                    DependencyStatus::getScenarioName,
                                                                    dependencyStatus -> dependencyStatus)));
                                        })
                                        .toList();
                                return new ScenarioDependency(YamlParser.convert(scenarioGroup.get("Сценарий")), items);
                            }).toList();
                    return new FunctionalityDependency(YamlParser.convert(functionalityGroup.get("Функционал")), scenarioDependencyList);
                }).toList();
    }

    public boolean haveDependency(List<Object> scenarioEntities) {
        FeatureWrapper featureWrapper = YamlParser.convert(scenarioEntities.get(1));
        PickleWrapper pickleWrapper = YamlParser.convert(scenarioEntities.get(0));
        String stringValue = featureWrapper.toString();
        String functionality = stringValue.substring(1, stringValue.length() - 1);
        String scenarioName = pickleWrapper.getPickle().getName();
        return scenarioDependencies.stream()
                .filter(item -> item.functionality().equals(functionality))
                .anyMatch(item -> item.scenarioDependencies().stream()
                        .anyMatch(scenarioDependency -> scenarioDependency.scenarioName().equals(scenarioName)));
    }

    public void addWaitingScenario(List<Object> scenarioEntity) {
        waitingScenarios.add(scenarioEntity);
    }

    public List<Object> getFirstIndependent() {
        List<Object> result = waitingScenarios.stream().filter(scenarioEntity -> {
            FeatureWrapper featureWrapper = YamlParser.convert(scenarioEntity.get(1));
            String tmp = featureWrapper.toString();
            String functionality = tmp.substring(1, tmp.length() - 1);
            PickleWrapper pickleWrapper = YamlParser.convert(scenarioEntity.get(0));
            String scenarioName = pickleWrapper.getPickle().getName();
            return scenarioDependencies.stream()
                    .filter(item -> item.functionality().equals(functionality))
                    .anyMatch(item -> item.scenarioDependencies().stream()
                            .anyMatch(scenarioDependency -> scenarioDependency.scenarioName().equals(scenarioName)
                                    && scenarioDependency.dependsOn().stream()
                                    .allMatch(dependencyItem -> dependencyItem.scenarios().entrySet().stream()
                                            .allMatch(entry -> {
                                                DependencyStatus status = entry.getValue();
                                                return status.getExpected().equalsIgnoreCase("завершён")
                                                        || status.getExpected().equalsIgnoreCase(status.getCurrent());
                                            }))));
        }).findFirst().orElse(null);
        if (result != null) {
            waitingScenarios.remove(result);
        }
        return result;
    }

    public void changeDependencyStatus(String functionality, String scenarioName, int status) {
        scenarioDependencies.forEach(functionalityGroup -> functionalityGroup.scenarioDependencies()
                .forEach(scenario -> scenario.dependsOn().stream()
                        .filter(dependencyItem -> dependencyItem.functionality().equals(functionality))
                        .forEach(dependencyItem -> {
                            String currentStatus = status == 1 ? "пройден" : "провален";
                            DependencyStatus dependencyStatus = dependencyItem.scenarios().get(scenarioName);
                            if (dependencyStatus != null) dependencyStatus.setCurrent(currentStatus);
                        })));
    }

    public boolean isWaiting() {
        return !waitingScenarios.isEmpty();
    }

}
