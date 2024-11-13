package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.TauLogger;

public class MenuManager extends StepManager {

    @Step("в меню '{menuName}' выбрать '{itemName}'")
    public void selectItem(String menuName, String itemName) {
        run(() -> {
            IHasMenu menu = getElement(menuName);
            menu.selectMenuItem(itemName);
        }, "Не удалось выбрать пункт '%s' в меню '%s'", itemName, menuName);
        TauLogger.report("В меню '%s' выбран пункт '%s'", menuName, itemName);
    }
}
