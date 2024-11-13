package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.RadioButtonManager;

import static rs.taukit.utils.BasicUtils.eval;

public class RadioButtonStep extends RadioButtonManager {

    @И("^установить переключатель \"(.+)\" в значение \"(.+)\"$")
    public void setRadioButton(String elementName, String value) {
        super.switchRadio(elementName, eval(value));
    }

}
