package rs.taukit.driver.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.element.playwright.FileDownload;
import rs.taukit.page.playwright.PlaywrightPageObject;
import rs.taukit.records.ApiResponse;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public abstract class PwBrowser implements IWebBrowser {
    protected final List<PlaywrightPageObject> pages = new ArrayList<>();
    protected Browser browser;
    protected BrowserContext context;
    protected int activeTabIndex;

    protected PwBrowser(BrowserType browserType, int index, boolean openPdf) {
        this.browser = launch(browserType);
        this.context = createContext(index, openPdf);
    }

    protected abstract Browser launch(BrowserType browserType);

    protected abstract BrowserContext createContext(int index, boolean openPdf);

    private PlaywrightPageObject initPage(String pageName) {
        return new PlaywrightPageObject(getPage(), pageName);
    }

    protected Page getPage() {
        return context.pages().get(activeTabIndex);
    }

    @Override
    public List<byte[]> takeScreenshots() {
        return context.pages().stream()
                .map(tab -> {
                    try {
                        tab.bringToFront();
                        return tab.screenshot((new Page.ScreenshotOptions().setFullPage(true)));
                    } catch (Exception e) {
                        TauLogger.warn("Не удалось сделать снимок текущей вкладки браузера", e);
                        return new byte[0];
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public void openNewTab() {
        context.newPage();
        activeTabIndex = context.pages().size() - 1;
        pages.add(null);
    }

    @Override
    public void closeCurrentTab() {
        context.pages().get(activeTabIndex).close();
        pages.remove(activeTabIndex);
        if (activeTabIndex == context.pages().size()) {
            activeTabIndex--;
        }
        setActiveTab(activeTabIndex);
    }

    @Override
    public boolean setActiveTab(int tabIndex) {
        if (tabIndex < context.pages().size()) {
            context.pages().get(tabIndex).bringToFront();
            activeTabIndex = tabIndex;
        }
        return context.pages().size() - 1 == tabIndex;
    }

    @Override
    public void navigate(String url) {
        getPage().navigate(url);
    }

    @Override
    public ApiResponse sendPost(String requestUrl, String content) {
        APIRequestContext apiRequestContext = context.request();
        APIResponse response = apiRequestContext.post(
                requestUrl,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json; charset=utf-8")
                        .setHeader("Accept", "application/json")
                        .setData(content));
        return new ApiResponse(response.ok(), response.status(), response.statusText(), response.text());
    }

    @Override
    public ApiResponse sendGet(String requestUrl) {
        APIResponse response = context.request().get((requestUrl));
        return new ApiResponse(response.ok(), response.status(), response.statusText(), response.text());
    }

    @Override
    public String getType() {
        return browser.browserType().name();
    }

    @Override
    public PlaywrightPageObject getPage(String pageName) {
        if (activeTabIndex == pages.size()) {
            pages.add(initPage(pageName));
        } else if (!getCurrentPage().getPageName().equals(pageName)) {
            pages.remove(activeTabIndex);
            pages.add(activeTabIndex, initPage(pageName));
        }
        return getCurrentPage();
    }

    public BrowserContext getBrowserContext() {
        return context;
    }

    @Override
    public boolean isPageOpen() {
        return activeTabIndex < pages.size();
    }

    @Override
    public PlaywrightPageObject getCurrentPage() {
        return pages.get(activeTabIndex);
    }

    @Override
    public void close() {
        context.close();
        browser.close();
    }

    @Override
    public void downloadByClick(FileDownload element, String fileName) {
        getPage().onDownload((pwDownload) -> {
            String dirName = FileUtils.getDownloadFilePath();
            File file = new File(dirName, fileName);
            pwDownload.saveAs(file.toPath());
            TauLogger.report("Загружен файл \"%s\" в папку \"%s\"", fileName, dirName);
        });
        double fileDownloadTimeout = TimeStringParser.getTime(Config.getProperty("file.download.timeout"));
        getPage().waitForDownload(new Page.WaitForDownloadOptions().setTimeout(fileDownloadTimeout), () -> {
            element.getElement().click();
            TauLogger.report("Нажали на кнопку \"%s\"", element.getElementName());
        });
    }

    public Page getActivePwPage() {
        return getPage();
    }
}
