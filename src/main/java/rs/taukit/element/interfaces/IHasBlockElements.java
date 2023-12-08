package rs.taukit.element.interfaces;

public interface IHasBlockElements extends IElement {
    IElement getElementByName(int blockNumber, String elementName);

    IElement getElementByName(String elementName);
}
