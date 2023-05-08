package rs.taukit.exceptions;

/**
 * Исключение "Ошибка при получении описания текущего состояния элемента".
 */
public class ElementStateNotFoundException extends RuntimeException {
    public ElementStateNotFoundException(String elementState) {
        super(String.format("Отсутствует описание для состояния '%s'", elementState));
    }

}
