package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.ToggleManager;

public class ToggleStep extends ToggleManager{

    @И("^включить переключатель \"(.+)\"$")
    public void turnOn(String fieldName) {
        super.turnOn(fieldName);
    }

    @И("^выключить переключатель \"(.+)\"$")
    public void turnOff(String fieldName) {
        super.turnOff(fieldName);
    }

}
