package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicMenuStep extends BasicUtils {
    private final String menuName;
    private final IHasMenu menu;

    public AllureBasicMenuStep(String menuName) {
        this.menuName = menuName;
        Class<IElement> elementType = ElementType.getType("меню").getElementClass();
        this.menu = page().replaceElement(menuName, elementType).castTo(IHasMenu.class);
    }

    @Step("в меню '{this.menuName}' выбрать '{itemName}'")
    public void selectItem(String itemName) {
        menu.selectMenuItem(itemName);
        TauLogger.report("В меню '%s' выбран пункт '%s'", menuName, itemName);
    }
}
