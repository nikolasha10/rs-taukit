package rs.taukit.element.interfaces;

import java.util.List;
import java.util.stream.Stream;

public interface IContainer extends IElement, HasText {
    List<IElement> getElementList();

    IElement getElementByName(String elementName);

    IElement getElementByText(String elementName);

    boolean containsElement(String elementName);

    Stream<IContainer> getStream();

    int getCurrentIndex();

    void setCurrentIndex(int index);
}
