package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.DictionaryManager;

import static rs.taukit.utils.BasicUtils.eval;

public class DictionaryStep extends DictionaryManager {

    @И("^в справочнике \"(.+)\" выбрать значение \"(.+)\"$")
    public void turnOn(String fieldName, String value) {
        super.selectValue(fieldName, eval(value));
    }

}
