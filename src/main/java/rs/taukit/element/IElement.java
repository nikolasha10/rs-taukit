package rs.taukit.element;

public interface IElement {
    <T> T castTo(Class<T> elementType);
    String getLocator();
    boolean isVisible();
    void click();

    Object getElementObject();
}
