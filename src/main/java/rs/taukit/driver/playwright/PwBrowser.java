package rs.taukit.driver.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import io.cucumber.datatable.DataTable;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.element.playwright.PwFileDownload;
import rs.taukit.page.playwright.PlaywrightPageObject;
import rs.taukit.records.ApiResponse;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.driver;

@Getter
public abstract class PwBrowser implements IWebBrowser {
    protected final List<PlaywrightPageObject> pages = new ArrayList<>();
    protected Browser browser;
    protected BrowserContext context;
    protected int activeTabIndex;
    private String userLogin;

    protected PwBrowser(BrowserType browserType, int index, boolean openPdf) {
        this.browser = launch(browserType);
        this.context = createContext(index, openPdf);
        setDefaultTimeout(Config.getTimeProperty("defaultTimeout"));
    }

    protected abstract Browser launch(BrowserType browserType);

    protected abstract BrowserContext createContext(int index, boolean openPdf);

    private PlaywrightPageObject initPage(String pageName) {
        try {
            return new PlaywrightPageObject(getPage(), pageName);
        } catch (Exception e) {
            TauLogger.console(e);
        }
        return null;
    }

    protected Page getPage() {
        return context.pages().get(activeTabIndex);
    }

    @Override
    public List<byte[]> takeScreenshots() {
        return context.pages().stream()
                .map(tab -> {
                    Exception exception = null;
                    for (int i = 0; i < 3; i++) {
                        try {
                            if (i > 0) tab.reload();
                            tab.waitForLoadState();
                            tab.bringToFront();
                            return tab.screenshot((new Page.ScreenshotOptions().setFullPage(true)));
                        } catch (Exception e) {
                            exception = e;
                        }
                    }
                    TauLogger.warnWOSS("Не удалось сделать снимок текущей вкладки браузера", exception);
                    return new byte[0];
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
    public void closeTab(int tabIndex) {
        if (tabIndex == activeTabIndex) {
            closeCurrentTab();
        } else {
            pages.remove(tabIndex);
            context.pages().get(tabIndex).close();
            if (tabIndex < activeTabIndex) {
                activeTabIndex--;
            }
        }
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
        driver().waitForCondition(() -> {
            try {
                getPage().navigate(url);
                return true;
            } catch (Exception e) {
                return false;
            }
        }, Config.getTimeProperty("pageLoadingTimeout"));
    }

    @Override
    public ApiResponse sendPost(String requestUrl, Map<String, String> headers, Map<String, String> queryParams, String content) {
        APIRequestContext apiRequestContext = context.request();
        RequestOptions requestOptions = RequestOptions.create().setData(content);
        headers.forEach(requestOptions::setHeader);
        queryParams.forEach(requestOptions::setQueryParam);
        APIResponse response = apiRequestContext.post(requestUrl, requestOptions);
        return new ApiResponse(response.ok(), response.status(), response.statusText(), response.text());
    }

    @Override
    public ApiResponse sendPut(String requestUrl, Map<String, String> headers, Map<String, String> queryParams, String content) {
        APIRequestContext apiRequestContext = context.request();
        RequestOptions requestOptions = RequestOptions.create().setData(content);
        headers.forEach(requestOptions::setHeader);
        queryParams.forEach(requestOptions::setQueryParam);
        APIResponse response = apiRequestContext.put(requestUrl, requestOptions);
        return new ApiResponse(response.ok(), response.status(), response.statusText(), response.text());
    }

    @Override
    public ApiResponse sendGet(String requestUrl, Map<String, String> headers, Map<String, String> queryParams) {
        RequestOptions requestOptions = RequestOptions.create();
        headers.forEach(requestOptions::setHeader);
        queryParams.forEach(requestOptions::setQueryParam);
        APIResponse response = context.request().get(requestUrl, requestOptions);
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
            pages.set(activeTabIndex, initPage(pageName));
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
        return activeTabIndex < pages.size() ? pages.get(activeTabIndex) : null;
    }

    @Override
    public void close() {
        context.close();
        browser.close();
    }

    @Override
    public void downloadByClick(PwFileDownload element, String fileName) {
        AtomicBoolean isDownloaded = new AtomicBoolean(false);
        Consumer<Download> download = pwDownload -> {
            if (isDownloaded.get()) return;
            String dirName = FileUtils.getDownloadFilePath();
            File file = new File(dirName, fileName);
            pwDownload.saveAs(file.toPath());
            TauLogger.report("Загружен файл \"%s\" в папку \"%s\"", fileName, dirName);
            isDownloaded.set(true);
        };
        getPage().onDownload(download);
        double fileDownloadTimeout = Config.getTimeProperty("file.download.timeout");
        getPage().waitForDownload(new Page.WaitForDownloadOptions().setTimeout(fileDownloadTimeout), () -> {
            element.getElementPwLocator().click();
            TauLogger.report("Нажали на кнопку \"%s\"", element.getElementName());
        });
    }

    @Override
    public void downloadByClick(PwFileDownload element, DataTable dataTable) {
        double fileDownloadTimeout = Config.getTimeProperty("file.download.timeout");
        String dirName = FileUtils.getDownloadFilePath();
        AtomicInteger waitTimes = new AtomicInteger(0);
        Page.WaitForDownloadOptions options = new Page.WaitForDownloadOptions().setTimeout(fileDownloadTimeout);
        Map<String, File> filesMap = new HashMap<>();
        dataTable.asMaps().forEach(data -> {
            String fileName = TestDataUtils.get(data, "Имя файла");
            String renameTo = TestDataUtils.get(data, "Новое имя файла");
            File file = new File(dirName, renameTo);
            filesMap.put(fileName, file);
        });
        Consumer<Download> download = pwDownload -> {
            waitTimes.getAndAdd(1);
            File file = filesMap.get(pwDownload.suggestedFilename());
            pwDownload.saveAs(file.toPath());
            TauLogger.report("Загружен файл \"%s\" в папку \"%s\"", file.getName(), dirName);
            try {
                driver().waitForCondition(() -> waitTimes.get() == filesMap.size());
            } catch (TimeoutError te) {
                getPage().waitForDownload(options, () -> TauLogger.console("Ожидаем загрузки следующего файла"));
            }
        };
        getPage().onDownload(download);
        getPage().waitForDownload(options, () -> {
            element.getElementPwLocator().click();
            TauLogger.report("Нажали на кнопку \"%s\"", element.getElementName());
        });
    }

    @Override
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public void closeOtherPages(int activePageIndex) {
        PlaywrightPageObject activePage = pages.get(activePageIndex);
        while (context.pages().size() > 1) {
            closeTab(0);
        }
        pages.set(0, activePage);
        setActiveTab(0);
        TauLogger.console("Все вкладки кроме 1-й закрыты");
    }

    public Page getActivePwPage() {
        return getPage();
    }

    @Override
    public void setDefaultTimeout(double timeProperty) {
        this.context.setDefaultTimeout(timeProperty);
    }
}
