package rs.taukit.driver;

import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.exceptions.DriverNotFoundException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverManager {
    private final Map<String, IWebDriver> drivers = new HashMap<>();
    private String currentDriverName;

    public void initDriver(String driverName) {
        IWebDriver driver;
        try {
            if (driverName.equals("playwright")) {
                driver = new PlaywrightDriver();
            } else {
                throw new RuntimeException();
            }
        } catch (IOException e) {
            throw new DriverNotFoundException(e, driverName);
        }
        drivers.put(driverName, driver);
    }

    public void getDriver(String driverName) {
        this.currentDriverName = driverName;
        if (!drivers.containsKey(driverName)) {
            initDriver(driverName);
        }
    }

    public IWebDriver getCurrentDriver() {
        return drivers.get(currentDriverName);
    }

    public void close() {
        drivers.values().forEach(IWebDriver::closeDriver);
    }
}
