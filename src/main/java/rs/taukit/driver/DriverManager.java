package rs.taukit.driver;

import rs.taukit.driver.playwright.PlaywrightDriver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverManager {
    private final Map<Long, IWebDriver> currentDriverContainer = new HashMap<>();

    private DriverManager() {
    }

    public static DriverManager getInstance() {
        return DriverManagerHolder.INSTANCE;
    }

    public IWebDriver initDriver() {
        long threadId = Thread.currentThread().threadId();
        PlaywrightDriver playwrightDriver;
        try {
            playwrightDriver = new PlaywrightDriver();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        IWebDriver driver = currentDriverContainer.getOrDefault(threadId, playwrightDriver);
        currentDriverContainer.putIfAbsent(threadId, driver);
        return driver;
    }

    public IWebDriver getDriver() {
        return currentDriverContainer.get(Thread.currentThread().threadId());
    }

    public void closeDrivers() {
        currentDriverContainer.forEach((key, value) -> value.closeDriver());
    }

    public long getDriverId(IWebDriver driver) {
        return currentDriverContainer.entrySet().stream().filter(entry -> entry.getValue().equals(driver))
                .findFirst().orElseThrow().getKey();
    }

    private static class DriverManagerHolder {
        public static final DriverManager INSTANCE = new DriverManager();
    }
}
