package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.MouseManager;

public class MouseStep extends MouseManager {
    @И("^навести указатель мыши на (.+) \"(.+)\"$")
    public void mouseOver(String elementType, String elementName){
        super.mouseOver(elementType, elementName);
    }
}
