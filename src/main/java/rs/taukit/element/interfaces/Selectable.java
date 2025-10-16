package rs.taukit.element.interfaces;

import rs.taukit.element.Element;

import java.util.List;

public interface Selectable {
    List<String> getOptions();

    Clickable getOptionByNumber(int number);
}