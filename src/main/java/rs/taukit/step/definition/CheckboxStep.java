package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.CheckboxManager;

import static rs.taukit.utils.BasicUtils.eval;

public class CheckboxStep extends CheckboxManager{

    @И("^установить чекбокс \"(.+)\"$")
    public void checkBox(String elementName) {
        check(eval(elementName));
    }

    @И("^снять чекбокс \"(.+)\"$")
    public void uncheckBox(String elementName) {
        uncheck(eval(elementName));
    }
}
