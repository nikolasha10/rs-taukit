package rs.taukit.page;

import rs.taukit.config.Config;
import rs.taukit.driver.DriverManager;
import rs.taukit.storage.StorageManager;
import rs.taukit.storage.TestContext;
import rs.taukit.utils.TimeStringParser;

import java.util.*;
import java.util.stream.Collectors;

public class PageStorage {
    private final ThreadLocal<Map<String, PageObject>> storage = new ThreadLocal<>();
    private final ThreadLocal<Map<String, Long>> timeIndexes = new ThreadLocal<>();

    public static PageStorage getInstance() {
        return PageStorageHolder.INSTANCE;
    }

    public PageObject getPage(String pageName) {
        addPage(pageName, DriverManager.getInstance().getDriver().initPage(pageName));
        Map<String, Long> localTimeIndexes = Optional.ofNullable(timeIndexes.get()).orElse(new HashMap<>());
        localTimeIndexes.put(pageName, new Date().getTime());
        timeIndexes.set(localTimeIndexes);
        return storage.get().get(pageName);
    }

    public PageObject getPopup(String pageName) {
        TestContext testContext = StorageManager.getInstance().getTestContext();
        testContext.setSavedPageName(testContext.getCurrentPageName());
        return getPage(pageName);
    }

    private synchronized void addPage(String pageName, PageObject page) {
        Map<String, PageObject> localStorage = Optional.ofNullable(storage.get()).orElse(new HashMap<>());
        localStorage.putIfAbsent(pageName, page);
        storage.set(localStorage);
        removeObsoletePages();
    }

    private void removeObsoletePages() {
        String removeObsoletePageAfter = Config.getProperty("remove.obsolete.page.after");
        double timeout = TimeStringParser.getTime(removeObsoletePageAfter);
        Map<String, Long> localTimeIndexes = Optional.ofNullable(timeIndexes.get()).orElse(new HashMap<>());
        Set<String> obsolete = localTimeIndexes.entrySet().stream()
                .filter(entry -> new Date().getTime() - entry.getValue() > timeout).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Map<String, PageObject> localStorage = Optional.ofNullable(storage.get()).orElse(new HashMap<>());
        obsolete.forEach(pageName -> {
            localStorage.remove(pageName);
            localTimeIndexes.remove(pageName);
        });
        storage.set(localStorage);
        timeIndexes.set(localTimeIndexes);
    }

    private static final class PageStorageHolder {
        private static final PageStorage INSTANCE = new PageStorage();
    }
}
