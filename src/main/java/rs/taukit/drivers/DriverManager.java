package rs.taukit.drivers;

import io.qameta.allure.Story;

import java.util.*;

public class DriverManager {
    private final Map<Long,WebDriver> currentDriverContainer = new HashMap<>();
    private DriverManager() {
    }

    private static class DriverManagerHolder {
        public static final DriverManager INSTANCE = new DriverManager();
    }

    public static DriverManager getInstance() {
        return DriverManagerHolder.INSTANCE;
    }

    public WebDriver initDriver() {
        long threadId = Thread.currentThread().threadId();
        WebDriver driver = currentDriverContainer.getOrDefault(threadId, new PlaywrightDriver());
        currentDriverContainer.putIfAbsent(threadId, driver);
        return driver;
    }

    public WebDriver getDriver() {
        return currentDriverContainer.get(Thread.currentThread().threadId());
    }
    public void closeDrivers(){
        currentDriverContainer.forEach((key, value) -> value.close());
    }
}
