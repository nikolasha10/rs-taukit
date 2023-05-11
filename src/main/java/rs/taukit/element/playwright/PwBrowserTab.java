package rs.taukit.element.playwright;

import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwBrowserTab extends PwElement implements IHasEditableValue {
    public PwBrowserTab(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
    }


    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getText() {
        return null;
    }

    public boolean isOpened(){
        return true;
    }
}
