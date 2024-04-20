package rs.taukit.records;

import java.util.Map;

public record TestParentSuiteResults(Map<String, TestSuiteResults> testSuiteResults) {
}
