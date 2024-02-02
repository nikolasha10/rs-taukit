package rs.taukit.records;

import java.util.List;

public record ScenarioDependency(String scenarioName, List<DependencyItem> dependsOn) {
}
