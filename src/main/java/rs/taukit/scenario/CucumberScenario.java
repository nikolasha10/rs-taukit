package rs.taukit.scenario;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.config.Config;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.AllureReport;
import rs.taukit.utils.TauLogger;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.storage;

@Getter
public class CucumberScenario {
    private final String functionality;
    private final String name;
    private final FeatureWrapper featureWrapper;
    private final PickleWrapper pickleWrapper;
    private final List<String> tagList;
    private final Map<String, String> dependencyMap;
    private final ScenarioStorage scenarioStorage = new ScenarioStorage();
    private final String suiteName;
    private final String groupName;
    @Setter
    private boolean started = false;
    @Setter
    private boolean skipped = false;
    private TestNGCucumberRunner runner;

    public CucumberScenario(FeatureWrapper featureWrapper, PickleWrapper pickleWrapper) {
        this.featureWrapper = featureWrapper;
        this.pickleWrapper = pickleWrapper;
        this.functionality = getFunctionality(featureWrapper);
        this.name = getName(pickleWrapper);
        this.tagList = pickleWrapper.getPickle().getTags();
        this.dependencyMap = Config.getDependencies().getScenarioDependencyMap(tagList);
        scenarioStorage.setScenarioData(new ScenarioData(pickleWrapper.getPickle()));
        String functionality = getFunctionality();
        Matcher matcher = Pattern.compile("\\[(.+)]\\[(.+)]").matcher(functionality);
        if (matcher.matches()) {
            this.suiteName = matcher.group(1);
            this.groupName = matcher.group(2);
        } else {
            this.suiteName = functionality;
            this.groupName = null;
        }
    }

    public CucumberScenario(Object[] scenarioObject) {
        this((FeatureWrapper) scenarioObject[1], (PickleWrapper) scenarioObject[0]);
    }

    public boolean isWaitingForDependency() {
        return !dependencyMap.isEmpty() && dependencyMap.entrySet().stream()
                .anyMatch(entry -> Config.getDependencies().checkStatus(entry.getValue(), entry.getKey()));
    }

    public boolean isReadyForStart() {
        return !isWaitingForDependency() && !started;
    }

    private String getName(PickleWrapper pickleWrapper) {
        return pickleWrapper.getPickle().getName();
    }

    private String getFunctionality(FeatureWrapper featureWrapper) {
        String functionalityName = featureWrapper.toString();
        return functionalityName.substring(1, functionalityName.length() - 1);
    }

    public void onStart(TestNGCucumberRunner runner) {
        this.runner = runner;
        scenarioStorage.getDriverManager().getDriver(Config.getProperty("web.driver.name"));
        TauLogger.setLogPath(functionality, name);
        started = true;
        System.out.println();
        TauLogger.console("TEST %s: %s %s", skipped ? "SKIPPED" : "STARTED", featureWrapper, name);
        if (!skipped) {
            runner.runScenario(getPickleWrapper().getPickle());
        }
    }

    public void onFinish(int statusCode, long endMillis) {
        storage.getScenarioList().remove(this);
        scenarioStorage.getDriverManager().close();
        String testStatus = switch (statusCode) {
            case 1 -> "PASSED";
            case 3 -> "SKIPPED";
            default -> "FAILED";
        };
        Config.getDependencies().changeStatus(tagList, testStatus.toLowerCase());
        TauLogger.console("TEST %s: %s %s", testStatus, functionality, name);
        AllureReport.editReports(name, functionality, testStatus.toLowerCase(), endMillis);
        TauLogger.collectLogs();
    }

    public boolean hasEqualName(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        return functionality.equals(getFunctionality(featureWrapper)) && name.equals(getName(pickleWrapper));
    }

    @Override
    public String toString() {
        String functionality = getFunctionality();
        Matcher matcher = Pattern.compile("\\[(.+)]\\[(.+)]").matcher(functionality);
        if (matcher.matches()) {
            String testSuiteName = matcher.group(1);
            String testGroupName = matcher.group(2);
            return String.format("{tags: %s, suite: %s, group: %s, name: %s}",
                    String.join(" ", tagList), testSuiteName, testGroupName, name);
        }
        return String.format("{tags: %s, suite: %s, name: %s}", String.join(" "), functionality, name);
    }
}
