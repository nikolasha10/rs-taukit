package rs.taukit.element;

public interface IHasEditableValue extends IElement{
    String setValue(String value);
    void setValue(String value, String controlValue);
    String getValue();

}
