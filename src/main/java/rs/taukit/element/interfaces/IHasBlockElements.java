package rs.taukit.element.interfaces;

import java.util.List;

public interface IHasBlockElements extends IElement {
    List<IElement> getElementList();
    IElement getElementByName(int blockNumber, String elementName);

    IElement getElementByName(String elementName);
    boolean containsElement(String elementName);
}
