package rs.taukit.records;

import java.util.Map;

public record DependencyItem(String functionality, Map<String, DependencyStatus> scenarios) {
}
