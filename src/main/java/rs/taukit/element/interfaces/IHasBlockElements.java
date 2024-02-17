package rs.taukit.element.interfaces;

import java.util.List;
import java.util.stream.Stream;

public interface IHasBlockElements extends IElement {
    List<IElement> getElementList();

    IElement getElementByName(int blockNumber, String elementName);

    IElement getElementByName(String elementName);

    IElement getElementByText(String elementName);

    boolean containsElement(String elementName);

    Stream<IHasBlockElements> getStream();

    int getCurrentIndex();

    void setCurrentIndex(int index);
}
