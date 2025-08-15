package rs.taukit.element.interfaces;
import java.util.List;

public interface Editable extends IElement, HasText {
    void setValue(String value);

    void setValue(String value, String controlValue);

    void clear();

    default List<String> getOptions() {
        throw new UnsupportedOperationException(
                "Метод getOptions() не поддерживается для типа '" + this.getClass().getSimpleName() + "'"
        );
    }
}