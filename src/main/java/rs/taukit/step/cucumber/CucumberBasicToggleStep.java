package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicToggleStep;

public class CucumberBasicToggleStep {
    private AllureBasicToggleStep toggle(String fieldName) {
        return new AllureBasicToggleStep(fieldName);
    }

    @И("^включить переключатель \"(.+)\"$")
    public void turnOn(String fieldName) {
        toggle(fieldName).turnOn();
    }

    @И("^выключить переключатель \"(.+)\"$")
    public void turnOff(String fieldName) {
        toggle(fieldName).turnOff();
    }

}
