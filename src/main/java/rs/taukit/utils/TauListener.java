package rs.taukit.utils;

import rs.taukit.config.Config;
import rs.taukit.scenario.CucumberScenario;

import java.util.Map;
import java.util.function.Consumer;

import static rs.taukit.utils.BasicUtils.storage;

public class TauListener {
    public static void afterInvocation(String methodName, int maxThreadCount, Runnable suite) {
        if (!methodName.equals("afterMethod")) {
            return;
        }
        int startedCount = storage.getScenarioList().stream().filter(CucumberScenario::isStarted).toList().size();
        if (storage.getScenarioList().isEmpty()) {
            if (!storage.isTestsFinished()) {
                storage.setTestsFinished(true);
                suite.run();
            }
        } else {
            if (startedCount < maxThreadCount) {
                suite.run();
            }
        }
    }

    public static void onStart(Map<String, String> parameters,
                               Consumer<Integer> setDataProviderThreadCount,
                               Consumer<Map<String, String>> setParameters) {
        if (parameters.isEmpty() || !parameters.containsKey("started")) {
            ClassLoader classLoader = TauListener.class.getClassLoader();
            Config.init("src/test/resources/configs");
            int threadCount = Integer.parseInt(Config.getProperty("threadCount"));
            setDataProviderThreadCount.accept(threadCount);
            parameters.put("started", "true");
            setParameters.accept(parameters);
        }

    }
}
