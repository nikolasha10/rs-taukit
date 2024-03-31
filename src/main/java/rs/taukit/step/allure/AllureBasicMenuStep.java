package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicMenuStep extends BasicUtils {
    private final String menuName;
    private final IHasMenu menu;

    public AllureBasicMenuStep(String menuName) {
        this.menuName = menuName;
        this.menu = page().getElement(menuName);
    }

    @Step("в меню '{this.menuName}' выбрать '{itemName}'")
    public void selectItem(String itemName) {
        try {
            menu.selectMenuItem(itemName);
            TauLogger.report("В меню \"%s\" выбран пункт \"%s\"", menuName, itemName);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось выбрать пункт '%s' в меню '%s'", itemName, menuName);
        }
    }
}
