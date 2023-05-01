package rs.taukit.driver.playwright;

import com.microsoft.playwright.*;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.driver.WebDriver;
import rs.taukit.page.PageObject;
import rs.taukit.page.playwright.PlaywrightPageObject;
import rs.taukit.storage.StorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

@Getter
public class PlaywrightDriver implements WebDriver {
    private final Playwright playwright;
    private final Browser browser;
    private final List<WebBrowser> browserList = new ArrayList<>();
    private BrowserContext browserContext;
    private int browserIndex;
    private Page page;

    public PlaywrightDriver() {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute(Config.getProperty("autotest.element.id"));
        BrowserType browserType = getBrowserType(playwright, Config.getProperty("browser.type"));
        browser = browserType.launch(new BrowserType.LaunchOptions()
                .setHeadless(Config.getBooleanProperty("browser.headless")));
    }

    private static BrowserType getBrowserType(Playwright playwright, String browserName) {
        switch (browserName) {
            case "chromium":
                return playwright.chromium();
            case "webkit":
                return playwright.webkit();
            case "firefox":
                return playwright.firefox();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void openBrowser() {
        browserContext = browser.newContext();
        page = browserContext.newPage();
        browserList.add(new WebBrowser(browserContext));
        browserIndex = browserList.size() - 1;
    }

    @Override
    public void reopenBrowser() {
        closeBrowser();
        openBrowser();
    }

    @Override
    public void switchBrowser(int browserIndex) {
        browserContext = browserList.get(browserIndex).context;
        this.browserIndex = browserIndex;
    }

    @Override
    public void switchBrowser(String browserName) {
        browserIndex = IntStream.range(0, browserList.size())
                .filter(index -> browserList.get(index).name.equalsIgnoreCase(browserName))
                .findFirst().orElseThrow();
        browserContext = browserList.get(browserIndex).context;
    }

    @Override
    public boolean isBrowserOpen() {
        return browser == null;
    }

    @Override
    public String getBrowserType() {
        return browser.browserType().name();
    }

    @Override
    public void closeBrowser() {
        browserContext.close();
        StorageManager.getInstance().getTestContext().setCurrentUser(null);
        browserList.remove(browserIndex);
    }

    @Override
    public PageObject initPage(String pageName) {
        return new PlaywrightPageObject(page, pageName);
    }

    @Override
    public String getCurrentPageUrl() {
        return page.url();
    }

    @Override
    public void closeDriver() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @Override
    public byte[] getScreenshot() {
        return page.screenshot();
    }

    public Locator getLocator(String locator) {
        return locator.startsWith("//") ? page.locator(locator) : page.getByTestId(locator);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition) {
        browserContext.waitForCondition(condition);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double interval, double timeout, BooleanSupplier previousStep) {
        AtomicBoolean isActual = new AtomicBoolean(true);
        BrowserContext.WaitForConditionOptions setTimeout = new BrowserContext.WaitForConditionOptions().setTimeout(timeout);
        browserContext.waitForCondition(() -> {
            try {
                BrowserContext.WaitForConditionOptions setInterval = new BrowserContext.WaitForConditionOptions().setTimeout(interval);
                browserContext.waitForCondition(condition, setInterval);
                return true;
            } catch (TimeoutError e) {
                if (isActual.get()) {
                    isActual.set(previousStep.getAsBoolean());
                }
                return false;
            }

        }, setTimeout);
    }

    private static class WebBrowser {
        public String name;
        public BrowserContext context;

        public WebBrowser(BrowserContext context) {
            this.context = context;
        }
    }
}
