package rs.taukit.driver;

import rs.taukit.element.playwright.FileDownload;
import rs.taukit.page.PageObject;
import rs.taukit.records.ApiResponse;

import java.util.List;

public interface IWebBrowser {
    String getType();

    PageObject getPage(String pageName);

    boolean isPageOpen();

    PageObject getCurrentPage();

    void close();

    List<byte[]> takeScreenshots();

    void openNewTab();

    void closeCurrentTab();

    boolean setActiveTab(int tabIndex);

    int getActiveTabIndex();

    void navigate(String url);

    ApiResponse sendPost(String requestUrl, String content);

    ApiResponse sendGet(String requestUrl);

    void downloadByClick(FileDownload element, String fileName);

    void closeTab(int tabIndex);

    String getUserLogin();

    void setUserLogin(String userLogin);

    void closeOtherPages(int activePageIndex);
}
