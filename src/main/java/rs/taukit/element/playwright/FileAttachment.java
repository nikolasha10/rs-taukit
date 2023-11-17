package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.storage.StorageManager;
import rs.taukit.testdata.TestData;
import rs.taukit.testdata.TestDataFile;

import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class FileAttachment extends PwElement implements IHasEditableValue {

    public FileAttachment(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public FileAttachment(String elementName, ElementModel model) {
        super(elementName, model);
    }
    public FileAttachment(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
    }
    public FileAttachment(String elementName, IElement element) {
        super(elementName, element);
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
