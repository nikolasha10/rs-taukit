package rs.taukit.driver.playwright;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.driver.IWebDriver;
import rs.taukit.utils.TauLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.scenarioStorage;

@Getter
public class PlaywrightDriver implements IWebDriver {
    private final Playwright playwright = Playwright.create();
    private final List<PwBrowser> browserList = new ArrayList<>();
    private int currentBrowserIndex;

    public PlaywrightDriver() throws IOException {
        playwright.selectors().setTestIdAttribute(Config.getProperty("autotest.element.id"));
    }

    private BrowserType getBrowserType(String browserName) {
        return switch (browserName) {
            case "chromium" -> playwright.chromium();
            case "webkit" -> playwright.webkit();
            case "firefox" -> playwright.firefox();
            default -> throw new IllegalArgumentException();
        };
    }

    private BrowserContext getBrowserContext() {
        return getCurrentBrowser().getBrowserContext();
    }

    @Override
    public void openBrowser() {
        openBrowser(false);
    }

    @Override
    public void openBrowser(boolean openPdf) {
        BrowserType browserType = getBrowserType(Config.getProperty("browser.type"));
        Chromium chromium = new Chromium(browserType, browserList.size(), openPdf);
        browserList.add(chromium);
        currentBrowserIndex = browserList.size() - 1;
    }

    @Override
    public void reopenBrowser() {
        closeBrowser();
        openBrowser();
    }

    @Override
    public void switchBrowser(int browserIndex) {
        this.currentBrowserIndex = browserIndex;
    }

    @Override
    public boolean isBrowserOpen() {
        return browserList.size() > currentBrowserIndex
                && getCurrentBrowser() != null
                && getCurrentBrowser().getBrowserContext() != null;
    }

    @Override
    public String getBrowserType() {
        return browserList.get(currentBrowserIndex).getType();
    }

    @Override
    public void closeBrowser() {
        browserList.get(currentBrowserIndex).close();
        scenarioStorage().setCurrentUser(null);
        browserList.remove(currentBrowserIndex);
    }


    @Override
    public void closeDriver() {
        playwright.close();
    }

    @Override
    public void waitForCondition(BooleanSupplier condition) {
        waitForCondition(condition, 30000);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double timeout) {
        BrowserContext.WaitForConditionOptions setTimeout = new BrowserContext.WaitForConditionOptions().setTimeout(timeout);
        AtomicReference<Boolean> conditionIsMet = new AtomicReference<>(false);
        getBrowserContext().waitForCondition(() -> {
            if (!conditionIsMet.get()) {
                conditionIsMet.set(condition.getAsBoolean());
            }
            return conditionIsMet.get();
        }, setTimeout);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double interval, double timeout, List<Runnable> previousStepList) {
        BrowserContext.WaitForConditionOptions setTimeout = new BrowserContext.WaitForConditionOptions().setTimeout(timeout);
        getBrowserContext().waitForCondition(() -> {
            try {
                BrowserContext.WaitForConditionOptions setInterval = new BrowserContext.WaitForConditionOptions().setTimeout(interval);
                getBrowserContext().waitForCondition(condition, setInterval);
                return true;
            } catch (TimeoutError e) {
                previousStepList.forEach(Runnable::run);
                return false;
            } catch (AssertionError ae) {
                return condition.getAsBoolean();
            }
        }, setTimeout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T castTo() {
        return (T) this;
    }

    public void waitForTimeout(double time) {
        try {
            Thread.sleep((long) time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PwBrowser getCurrentBrowser() {
        return browserList.get(currentBrowserIndex);
    }

    @Override
    public void closeOtherBrowsers(int activeBrowserIndex) {
        if (browserList.isEmpty()) return;
        int i = 0;
        while (browserList.size() > 1 && i < browserList.size()) {
            if (i == activeBrowserIndex) {
                i++;
            } else {
                TauLogger.console("Закрываем браузер %d", i);
                browserList.get(i).close();
                browserList.remove(i);
            }
            currentBrowserIndex = activeBrowserIndex;
        }
        TauLogger.console("Все браузеры кроме 1-го закрыты");
    }
}
