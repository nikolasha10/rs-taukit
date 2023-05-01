package rs.taukit.page;

import rs.taukit.config.Config;
import rs.taukit.driver.DriverManager;
import rs.taukit.storage.StorageManager;
import rs.taukit.storage.TestContext;
import rs.taukit.utils.TimeStringParser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PageStorage {
    private final Map<String, PageObject> storage = new HashMap<>();
    private final Map<String, Long> timeIndexes = new HashMap<>();

    public static PageStorage getInstance() {
        return PageStorageHolder.INSTANCE;
    }

    public PageObject getPage(String pageName) {
        addPage(pageName, DriverManager.getInstance().getDriver().initPage(pageName));
        timeIndexes.put(pageName, new Date().getTime());
        return storage.get(pageName);
    }

    public PageObject getPopup(String pageName) {
        TestContext testContext = StorageManager.getInstance().getTestContext();
        testContext.setSavedPageName(testContext.getCurrentPageName());
        return getPage(pageName);
    }

    private synchronized void addPage(String pageName, PageObject page) {
        storage.putIfAbsent(pageName, page);
        removeObsoletePages();
    }

    private void removeObsoletePages() {
        String removeObsoletePageAfter = Config.getProperty("remove.obsolete.page.after");
        double timeout = TimeStringParser.getTime(removeObsoletePageAfter);
        Set<String> obsolete = timeIndexes.entrySet().stream()
                .filter(entry -> new Date().getTime() - entry.getValue() > timeout).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        obsolete.forEach(pageName -> {
            storage.remove(pageName);
            timeIndexes.remove(pageName);
        });
    }

    private static final class PageStorageHolder {
        private static final PageStorage INSTANCE = new PageStorage();
    }
}
