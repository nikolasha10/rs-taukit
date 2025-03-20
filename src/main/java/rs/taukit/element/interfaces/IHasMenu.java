package rs.taukit.element.interfaces;

public interface IHasMenu extends IElement {
    void selectMenuItem(String itemName);

    boolean containsItem(String itemName);
}
