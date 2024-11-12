package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.CheckboxManager;

public class CheckboxStep extends CheckboxManager{

    @И("^установить чекбокс \"(.+)\"$")
    public void checkBox(String elementName) {
        check(elementName);
    }

    @И("^снять чекбокс \"(.+)\"$")
    public void uncheckBox(String elementName) {
        uncheck(elementName);
    }
}
