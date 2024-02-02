package rs.taukit.records;

import java.util.List;

public record FunctionalityDependency(String functionality, List<ScenarioDependency> scenarioDependencies) {
}
