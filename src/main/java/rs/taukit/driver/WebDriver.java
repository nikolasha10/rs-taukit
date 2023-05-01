package rs.taukit.driver;

import rs.taukit.page.PageObject;

import java.util.function.BooleanSupplier;

public interface WebDriver {
    void openBrowser();

    void reopenBrowser();

    void switchBrowser(int browserIndex);

    void switchBrowser(String browserName);

    boolean isBrowserOpen();

    String getBrowserType();

    void closeDriver();

    void closeBrowser();

    PageObject initPage(String pageName);

    String getCurrentPageUrl();

    byte[] getScreenshot();

    void waitForCondition(BooleanSupplier condition);
    void waitForCondition(BooleanSupplier condition, double interval, double timeout, BooleanSupplier previousStep);
}
