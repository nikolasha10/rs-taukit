package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasAttachment;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class FileAttachment extends PwElement implements IHasAttachment, IHasEditableValue {

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
    public void setValue(String filePath) {
        attach(filePath);
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void attach(String fileBaseDir, String... pathChunk) {
        element.setInputFiles(Paths.get(fileBaseDir, pathChunk));
    }
}
