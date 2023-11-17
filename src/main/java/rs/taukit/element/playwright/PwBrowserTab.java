package rs.taukit.element.playwright;

import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwBrowserTab extends PwElement implements IHasEditableValue {
    public PwBrowserTab(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwBrowserTab(String elementName, ElementModel model) {
        super(elementName, model);
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getText() {
        return null;
    }

    public boolean isOpened() {
        return true;
    }
}
