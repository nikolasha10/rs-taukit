package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.MenuManager;

public class MenuStep extends MenuManager{

    @И("^в меню \"(.+)\" выбрать \"(.+)\"$")
    public void selectItem(String menuName, String itemName) {
        super.selectItem(menuName, itemName);
    }
}
