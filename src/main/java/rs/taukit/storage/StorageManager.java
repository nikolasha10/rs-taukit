package rs.taukit.storage;

import io.cucumber.testng.PickleWrapper;

import java.util.HashMap;
import java.util.Map;

public class StorageManager {
    private final Map<Long, TestContext> storage = new HashMap<>();

    public static StorageManager getInstance() {
        return StorageManagerHolder.INSTANCE;
    }

    public TestContext initTestContext(PickleWrapper pickleWrapper, String functionality) {
        long threadId = Thread.currentThread().threadId();
        TestContext testContext = storage.getOrDefault(threadId, new TestContext(pickleWrapper, functionality));
        storage.putIfAbsent(threadId, testContext);
        testContext.update(pickleWrapper, functionality);
        return testContext;
    }

    public TestContext getTestContext() {
        return storage.get(Thread.currentThread().threadId());
    }

    private static class StorageManagerHolder {
        public static final StorageManager INSTANCE = new StorageManager();
    }
}
