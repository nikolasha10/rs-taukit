package step.allure;

import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.playwright.PwMenu;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class Menu extends BasicUtils {
    private final String menuName;
    private final PwMenu menu;

    public Menu(String menuName) {
        this.menuName = menuName;
        menu = element(menuName);
    }

    @Step("через '{this.menuName}' перейти на страницу '{pageName}'")
    public void selectMenuItem(String pageName) {
        AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
        BooleanSupplier supplier = () -> {
            try {
                menu.selectMenuItem(pageName);
                return true;
            } catch (Exception e){
                exceptionAtomicReference.set(e);
                return false;
            }
        };
        try {
            browser().setDefaultTimeout(Config.getTimeProperty("element.check.interval"));
            driver().waitForCondition(supplier);
            browser().setDefaultTimeout(Config.getTimeProperty("defaultTimeout"));
            TauLogger.report("В меню \"%s\" выбран пункт \"%s\"", menuName, pageName);
            driver().getCurrentBrowser().getPage(pageName).waitForLoad();
            TauLogger.report("Отображается страница \"%s\"", pageName);
        } catch (Exception e){
            if (exceptionAtomicReference.get() != null){
                e.addSuppressed(exceptionAtomicReference.get());
            }
            TauLogger.fail(e,"Не удалось выбрать пункт '%s' в меню '%s'", pageName, menuName);
        }
    }
}
