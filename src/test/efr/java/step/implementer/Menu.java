package step.implementer;

import io.qameta.allure.Step;
import rs.taukit.element.playwright.PwMenu;
import rs.taukit.step.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class Menu extends BasicUtils {
    private final String menuName;
    private final PwMenu menu;
    public Menu(String menuName) {
        this.menuName = menuName;
        menu = element(menuName).castTo(PwMenu.class);
    }

    @Step("Через '{this.menuName}' перейти на страницу '{pageName}'")
    public void selectMenuItem(String pageName) {
        menu.selectMenuItem(pageName);
        TauLogger.report("В меню '%s' выбран пункт '%s'", menuName, pageName);
        pageStorage().getPage(pageName).waitForLoad();
        TauLogger.report("Отображается страница '%s'", pageName);
    }
}
