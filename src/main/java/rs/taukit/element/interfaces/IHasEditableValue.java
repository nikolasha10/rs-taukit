package rs.taukit.element.interfaces;

public interface IHasEditableValue extends IElement{
    void setValue(String value);
    void setValue(String value, String controlValue);
    String getText();

}
