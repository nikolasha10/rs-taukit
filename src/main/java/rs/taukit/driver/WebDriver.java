package rs.taukit.driver;

import rs.taukit.page.PageObject;

import java.util.List;
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

    void waitForCondition(BooleanSupplier condition);
    void waitForCondition(BooleanSupplier condition, double interval, double timeout, BooleanSupplier previousStep);

    <T> T castTo();

    List<byte[]> takeScreenshots();

    void openNewTab();
    boolean setActiveTab(int tabIndex);

    void saveDownloadedFileAs(String originalFileName, String fileName);

    void navigate(String url);
}
