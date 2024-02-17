package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasAttachment;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class PwFileAttachment extends PwElement implements IHasAttachment, IHasEditableValue {

    public PwFileAttachment(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwFileAttachment(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwFileAttachment(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwFileAttachment(String elementName, IElement element) {
        super(elementName, element);
    }

    @Override
    public String getSelector() {
        return selector;
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
        elementPwLocator.setInputFiles(Paths.get(fileBaseDir, pathChunk));
    }
}
