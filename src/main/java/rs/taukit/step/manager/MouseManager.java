package rs.taukit.step.manager;

import io.qameta.allure.Step;

public class MouseManager extends StepManager {

    @Step("навести указатель мыши на {elementType} '{elementName}'")
    protected void mouseOver(String elementType, String elementName) {
        run(() -> getPage().getElement(elementName).hover(), """
                Не удалось навести указатель мыши на %s '%s' на странице '%s'
                """, elementType, elementName, getPage().getPageName());
    }
}
