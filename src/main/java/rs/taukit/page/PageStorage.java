package rs.taukit.page;

import static rs.taukit.utils.BasicUtils.driver;
import static rs.taukit.utils.BasicUtils.scenarioStorage;

public class PageStorage {

    public static PageStorage getInstance() {
        return PageStorageHolder.INSTANCE;
    }

    public PageObject getPage(String pageName) {
        return driver().getCurrentBrowser().getPage(pageName);
    }

    public PageObject getPopup(String pageName) {
        scenarioStorage().setSavedPageName(scenarioStorage().getCurrentPageName());
        return getPage(pageName);
    }


    private static final class PageStorageHolder {
        private static final PageStorage INSTANCE = new PageStorage();
    }
}
