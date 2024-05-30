package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.config.Config;

import java.io.File;
import java.util.LinkedHashMap;

@Getter
public class TestDataFile implements ITestData {
    private final String fieldName;
    private final DataStorage dataStorage;
    private final String filePath;
    private final String fileName;
    private final String fileExtension;

    public TestDataFile(String fieldName, LinkedHashMap<String, ?> data) {
        this(fieldName, data, "");
    }

    public TestDataFile(String fieldName, LinkedHashMap<String, ?> data, String dirPath) {
        this.fieldName = fieldName;
        this.dataStorage = new DataStorage(data);
        String filePath = Config.eval(this.dataStorage.get("путь"));
        File file = new File(dirPath, filePath);
        if (!file.exists()) {
            file = new File(filePath);
        }
        this.filePath = file.getPath().replaceAll("\\\\", "/");
        this.fileName = file.getName();
        String[] chunks = fileName.split("\\.");
        this.fileExtension = chunks[chunks.length - 1];
    }

    @Override
    public LinkedHashMap<String, Object> getAttachments() {
        return dataStorage.getAttachments(fileName);
    }

    @Override
    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    @Override
    public <T> T getOptional(String fieldName) {
        return dataStorage.getOptional(fieldName);
    }

    @Override
    public String getStringValue() {
        return fileName;
    }
}
