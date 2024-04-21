package rs.taukit.testdata;

import java.util.LinkedHashMap;

public interface ITestData {
    LinkedHashMap<String, Object> getAttachments();
    <T> T get(String fieldName);

    <T> T getOptional(String fieldName);

    String getStringValue();
}
