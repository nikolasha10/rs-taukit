package rs.taukit.element.interfaces;

public interface IHasEditableValue extends IElement, IHasText {
    void setValue(String value);

    void setValue(String value, String controlValue);
}
