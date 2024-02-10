package rs.taukit.storage;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import lombok.Getter;
import rs.taukit.exceptions.ScenarioNotFoundException;
import rs.taukit.scenario.CucumberScenario;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private final ThreadLocal<CucumberScenario> scenarioThreadLocal = new ThreadLocal<>();
    @Getter
    private final List<CucumberScenario> scenarioList = new ArrayList<>();

    public static Storage getInstance() {
        return StorageManagerHolder.INSTANCE;
    }

    public CucumberScenario getScenario() {
        return scenarioThreadLocal.get();
    }

    /**
     * Ищет объект сценария из списка, сформированного при запуске тестов, по данным из pickleWrapper и featureWrapper
     *
     * @param pickleWrapper  объект, содержащий данные запускаемого сценария cucumber
     * @param featureWrapper объект, содержащий данные функционала, к которому относится запускаемый сценарий
     * @return объект класса CucumberScenario из списка сценариев, сформированного при запуске тестов
     */
    public CucumberScenario getScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        return scenarioList.stream()
                .filter(scenario -> scenario.hasEqualName(pickleWrapper, featureWrapper))
                .findFirst().orElseThrow(() -> new ScenarioNotFoundException(pickleWrapper.getPickle().getName()));
    }

    public CucumberScenario putScenario(CucumberScenario scenario) {
        scenarioThreadLocal.set(scenario);
        return scenario;
    }

    public void remove(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        scenarioList.removeIf(scenario -> scenario.hasEqualName(pickleWrapper, featureWrapper));
    }

    private static class StorageManagerHolder {
        public static final Storage INSTANCE = new Storage();
    }
}
