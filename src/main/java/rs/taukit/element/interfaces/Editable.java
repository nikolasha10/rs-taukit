package rs.taukit.element.interfaces;

public interface Editable extends IElement, HasText {
    void setValue(String value);

    void setValue(String value, String controlValue);
}
