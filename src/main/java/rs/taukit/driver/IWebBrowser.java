package rs.taukit.driver;

import io.cucumber.datatable.DataTable;
import rs.taukit.element.playwright.PwFileDownload;
import rs.taukit.page.PageObject;
import rs.taukit.records.ApiResponse;

import java.util.List;
import java.util.Map;

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

    ApiResponse sendPostJson(String requestUrl, String content);

    ApiResponse sendPutJson(String requestUrl, String content);

    ApiResponse sendPost(String requestUrl, String contentType, String accept, String content);

    ApiResponse sendPut(String requestUrl, Map<String, String> headers, String content);

    ApiResponse sendGet(String requestUrl, Map<String, String> headers);

    void downloadByClick(PwFileDownload element, String fileName);

    void downloadByClick(PwFileDownload element, DataTable dataTable);

    void closeTab(int tabIndex);

    String getUserLogin();

    void setUserLogin(String userLogin);

    void closeOtherPages(int activePageIndex);

    void setDefaultTimeout(double timeProperty);
}
