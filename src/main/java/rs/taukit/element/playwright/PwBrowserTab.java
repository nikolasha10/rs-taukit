package rs.taukit.element.playwright;

import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.CanBeOpened;
import rs.taukit.element.interfaces.Editable;

import java.util.LinkedHashMap;

import static rs.taukit.utils.BasicUtils.browser;

public class PwBrowserTab implements CanBeOpened {
    @Override
    public boolean isOpened(String number) {
        int index = Integer.parseInt(number) - 1;
        return browser().setActiveTab(index);
    }
}
