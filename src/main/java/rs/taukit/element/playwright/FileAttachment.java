package rs.taukit.element.playwright;

import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.storage.StorageManager;
import rs.taukit.testdata.TestData;
import rs.taukit.testdata.TestDataFile;

import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class FileAttachment extends PlaywrightElement implements IHasEditableValue {

    public FileAttachment(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName,elementData);
    }

    @Override
    public String getLocator() {
        return locator;
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public void setValue(String value) {
        TestData testData = StorageManager.getInstance().getTestContext().getScenarioData().get("Клиент");
        TestDataFile testDataFile = testData.get("аватар");
        element.setInputFiles(Paths.get(testDataFile.getFilePath()));
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getText() {
        return null;
    }
}
