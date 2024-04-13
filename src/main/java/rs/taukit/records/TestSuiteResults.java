package rs.taukit.records;

import java.util.Map;

public record TestSuiteResults(Map<String, TestGroupResults> testGroupResults) {
}
