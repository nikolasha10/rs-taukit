package rs.taukit.exceptions;

/**
 * Исключение "Не найден тип элемента".
 */
public class ElementTypeNotFoundException extends RuntimeException {

    public ElementTypeNotFoundException(String typeName) {
        super(String.format("Тип элемента \"%s\" не найден", typeName));
    }

}
