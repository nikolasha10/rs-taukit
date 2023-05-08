package rs.taukit.driver.playwright;

import com.microsoft.playwright.*;
import io.qameta.allure.Step;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.driver.WebDriver;
import rs.taukit.element.playwright.FileDownload;
import rs.taukit.page.PageObject;
import rs.taukit.page.playwright.PlaywrightPageObject;
import rs.taukit.step.utils.BasicUtils;
import rs.taukit.storage.StorageManager;
import rs.taukit.utils.TauLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Getter
public class PlaywrightDriver implements WebDriver {
    private final Playwright playwright;
    private final Browser browser;
    private final List<WebBrowser> browserList = new ArrayList<>();
    private final File chromeDirExternally;
    private final File chromeDirInternally;
    private final String browserExtensionPath;
    private BrowserContext browserContext;
    private int browserIndex;
    private int pageIndex;
    private Download download;

    public PlaywrightDriver() throws IOException {
        chromeDirExternally = new File("target/chromium/open_pdf_externally");
        File defaultDirExternally = new File(chromeDirExternally.getPath(), "Default");
        File preferencesExternally = new File(defaultDirExternally.getPath(), "Preferences");
        if (!preferencesExternally.exists()) {
            Files.createDirectories(defaultDirExternally.toPath());
            Files.writeString(preferencesExternally.toPath(),
                    "{\"plugins\":{\"always_open_pdf_externally\":true}}", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        chromeDirInternally = new File("target/chromium/open_pdf_internally");
        File defaultDirInternally = new File(chromeDirInternally.getPath(), "Default");
        File preferencesInternally = new File(defaultDirInternally.getPath(), "Preferences");
        if (!preferencesInternally.exists()) {
            Files.createDirectories(defaultDirInternally.toPath());
            Files.writeString(preferencesInternally.toPath(),
                    "{\"plugins\":{\"always_open_pdf_externally\":false}}", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute(Config.getProperty("autotest.element.id"));
        BrowserType browserType = getBrowserType(playwright, Config.getProperty("browser.type"));
//        Map<String, Object> firefoxLaunchOptions = new HashMap<>();
//        firefoxLaunchOptions.put("pdfjs.disabled", false);
        browserExtensionPath = new File("browserExtensions/chromium").getAbsolutePath();
        List<String> args = Arrays.asList(
                "--load-extension=" + browserExtensionPath,
                "--disable-extensions-except=" + browserExtensionPath,
                "--embedded-extension-options=true");

        browser = browserType.launch(new BrowserType.LaunchOptions().setArgs(args)
//                .setExecutablePath(new File("C:/Program Files (x86)/Mozilla Firefox/firefox.exe").toPath())
//                .setFirefoxUserPrefs(firefoxLaunchOptions)
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

    public Page getPage() {

        return browserContext.pages().get(pageIndex);
    }

    @Override
    public void openBrowser() {
        openBrowser(false);
    }

    public void openBrowser(boolean openPdf) {
        List<String> args = new ArrayList<>();
        File persistentContext = chromeDirExternally;
        if (openPdf){
            args.addAll(Arrays.asList(
                    "--load-extension=" + browserExtensionPath,
                    "--disable-extensions-except=" + browserExtensionPath,
                    "--embedded-extension-options=true"));
            persistentContext = chromeDirInternally;
        }
//        browserContext = browser.newContext(new Browser.NewContextOptions().setAcceptDownloads(true));
        browserContext = browser.browserType()
                .launchPersistentContext(persistentContext.getAbsoluteFile().toPath(), new BrowserType.LaunchPersistentContextOptions()
                        .setArgs(args)
                        .setHeadless(Config.getBooleanProperty("browser.headless")));
//        if (browserContext.pages().size() == 0) {
//            browserContext.newPage();
//        }
        browserList.add(new WebBrowser(browserContext));
        browserIndex = browserList.size() - 1;
        pageIndex = browserContext.pages().size() - 1;
        getPage().setViewportSize(1280, 840);
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
        browserList.get(browserIndex).close();
        StorageManager.getInstance().getTestContext().setCurrentUser(null);
        browserList.remove(browserIndex);
    }

    @Override
    public PageObject initPage(String pageName) {
        return new PlaywrightPageObject(getPage(), pageName);
    }

    @Override
    public String getCurrentPageUrl() {
        return getPage().url();
    }

    @Override
    public void closeDriver() {
        browserList.forEach(item -> {
            item.close();
            TauLogger.console("Закрыт контекст");
        });
        browser.close();
        TauLogger.console("Закрыт браузер");
        if (playwright != null) {
            playwright.close();
            TauLogger.console("Закрыт драйвер");
        }
    }

    @Override
    public List<byte[]> takeScreenshots() {
        return browserContext.pages().stream()
                .map(tab -> {
                    try {
                        tab.bringToFront();
                        return tab.screenshot((new Page.ScreenshotOptions().setFullPage(true)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new byte[0];
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void openNewTab() {
        browserContext.newPage();
        pageIndex = browserContext.pages().size() - 1;
    }

    @Override
    public boolean setActiveTab(int tabIndex) {
        boolean result = browserContext.pages().size() > tabIndex;
        if (result) {
            pageIndex = tabIndex;
            browserContext.pages().get(pageIndex).bringToFront();
        }
        return result;
    }

    public Locator getLocator(String locator) {
        return locator.contains("/") ? getPage().locator(locator) : getPage().getByTestId(locator);
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
                    isActual.set(previousStep != null && previousStep.getAsBoolean());
                }
                return false;
            }

        }, setTimeout);
    }

    @Override
    public <T> T castTo() {
        return (T) this;
    }

    public void downloadByClick(FileDownload element) {
        getPage().onDownload((df) -> {
            String dirName = String.format("%s/%s",
                    Config.getProperty("downloads.dir"), BasicUtils.testContext().getScenarioName());
            File file = new File(dirName, df.suggestedFilename());
            df.saveAs(file.toPath());
            TauLogger.report("Загружен файл %s", df.suggestedFilename());
        });
        getPage().waitForDownload(new Page.WaitForDownloadOptions().setTimeout(60000), () -> {
            element.getElement().click();
            TauLogger.report("Нажали на кнопку '%s'", element.getElementName());
        });
    }

    @Step("сохранить файл с именем '{path}'")
    public void saveDownloadedFileAs(String originalFileName, String path) {
        String originalFilePath = String.format("%s/%s/%s",
                Config.getProperty("downloads.dir"),
                BasicUtils.testContext().getScenarioName(),
                originalFileName);
        String filePath = String.format("%s/%s/%s",
                Config.getProperty("downloads.dir"),
                BasicUtils.testContext().getScenarioName(),
                path);
        File file = new File(originalFilePath);
        File destinationFile = new File(filePath);
        try {
            Files.move(file.toPath(), destinationFile.toPath(), REPLACE_EXISTING);
            TauLogger.report("Файл сохранен в '%s'", destinationFile.getPath());
            TauLogger.attachFile(path, "pdf", destinationFile.getPath());
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось сохранить файл '%s', как '%s'", originalFileName, path);
        }
    }

    private static class WebBrowser {
        public String name;
        public BrowserContext context;

        public WebBrowser(BrowserContext context) {
            this.context = context;
        }

        public void close() {
            context.close();
        }
    }
}
