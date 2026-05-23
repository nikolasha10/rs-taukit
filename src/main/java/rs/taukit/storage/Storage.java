package rs.taukit.storage;

import lombok.Getter;
import rs.taukit.records.Mutex;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.testdata.DataStorage;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class Storage {
    private final ThreadLocal<CucumberScenario> scenarioThreadLocal = new ThreadLocal<>();
    private final DataStorage dataStorage = new DataStorage(new HashMap<>());
    @Getter
    private final Map<String, Mutex> semaphores = new HashMap<>();
    @Getter
    private final Stack<Map<String, ?>> errorStack = new Stack<>();

    public static Storage getInstance() {
        return StorageManagerHolder.INSTANCE;
    }

    public CucumberScenario getScenario() {
        return scenarioThreadLocal.get();
    }

    public void putScenario(CucumberScenario scenario) {
        scenarioThreadLocal.set(scenario);
    }

    public <T> T get(String key) {
        Optional<T> optional = scenarioData().get(key);
        return optional.orElse(dataStorage.get(key));
    }

    public void put(String key, Object value) {
        dataStorage.put(key, value);
    }

    public String evalObjectField(String template) {
        try {
            return scenarioData().evalObjectField(template);
        } catch (Exception e) {
            try {
                return dataStorage.evalObjectField(template);
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw e2;
            }
        }
    }

    public <T> T evalObject(String template) {
        try {
            return scenarioData().evalObject(template);
        } catch (Exception ignored) {
            return dataStorage.evalObject(template);
        }
    }

    public String evalString(String template) {
        try {
            return scenarioData().evalString(template);
        } catch (Exception e) {
            try {
                return dataStorage.evalString(template);
            } catch (Exception ignored) {
                throw e;
            }
        }
    }

    public synchronized void pushErrorData(Map<String, ?> errorData) {
        Map<String, ?> found = getErrorData(
                errorData.get("Автотест").toString(),
                errorData.get("Шаг").toString(),
                errorData.get("Ошибка").toString());
        if (found == null) {
            errorStack.push(errorData);
        }
    }

    public Map<String, ?> getErrorData(String scenarioName, String stepName, String errorMessage) {
        Predicate<Map<String, ?>> predicate = item -> item.get("Автотест").equals(scenarioName)
                && item.get("Шаг").equals(stepName)
                && item.get("Ошибка").equals(errorMessage);
        return errorStack.stream().filter(predicate).findFirst().orElse(null);
    }

    public void saveErrorData(File file) {
        FileUtils.createDirectories(file.getParent());
        FileUtils.writeFile(file.toPath(), YamlParser.write(errorStack, false));
    }

    private static class StorageManagerHolder {
        public static final Storage INSTANCE = new Storage();
    }

}
