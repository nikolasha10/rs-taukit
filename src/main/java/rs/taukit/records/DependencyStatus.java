package rs.taukit.records;

import lombok.Getter;
import lombok.Setter;

@Getter
public class DependencyStatus {
    private final String scenarioName;
    private final String expected;
    @Setter
    private String current;
    public DependencyStatus(String scenarioName, String expected) {
        this.scenarioName = scenarioName;
        this.expected = expected;
    }
}
