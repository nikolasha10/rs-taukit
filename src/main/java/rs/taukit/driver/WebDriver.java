package rs.taukit.driver;

import rs.taukit.page.PageObject;

import java.util.List;
import java.util.function.BooleanSupplier;

public interface WebDriver {
    void openBrowser();

    void openBrowser(boolean viewPdf);

    void reopenBrowser();

    void switchBrowser(int browserIndex);

    void switchBrowser(String browserName);

    boolean isBrowserOpen();

    String getBrowserType();

    void closeDriver();

    void closeBrowser();

    PageObject initPage(String pageName);

    String getCurrentPageUrl();

    void waitForCondition(BooleanSupplier condition);

    void waitForCondition(BooleanSupplier condition, double searchValueTimeout);

    void waitForCondition(BooleanSupplier condition, double interval, double timeout, BooleanSupplier previousStep);

    <T> T castTo();

    List<byte[]> takeScreenshots();

    void openNewTab();

    boolean setActiveTab(int tabIndex);

    void navigate(String url);

    long getDriverId();

    void waitForTimeout(double time);


}
