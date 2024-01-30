package rs.taukit.page;

import rs.taukit.storage.StorageManager;
import rs.taukit.storage.TestContext;

import static rs.taukit.utils.BasicUtils.driver;

public class PageStorage {

    public static PageStorage getInstance() {
        return PageStorageHolder.INSTANCE;
    }

    public PageObject getPage(String pageName) {
        return driver().getCurrentBrowser().getPage(pageName);
    }

    public PageObject getPopup(String pageName) {
        TestContext testContext = StorageManager.getInstance().getTestContext();
        testContext.setSavedPageName(testContext.getCurrentPageName());
        return getPage(pageName);
    }


    private static final class PageStorageHolder {
        private static final PageStorage INSTANCE = new PageStorage();
    }
}
