package rs.taukit.records;

import java.util.Map;

public record TestSubSuiteResults(Map<String, TestResults> testResults) {
}
