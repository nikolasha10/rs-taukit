package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicMenuStep;

public class CucumberBasicMenuStep {
    private AllureBasicMenuStep menuStep(String menuName) {
        return new AllureBasicMenuStep(menuName);
    }

    @И("^в меню \"(.+)\" выбрать \"(.+)\"$")
    public void selectItem(String menuName, String itemName) {
        menuStep(menuName).selectItem(itemName);
    }
}
