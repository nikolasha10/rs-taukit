package rs.taukit.exceptions;

import java.util.function.Supplier;

/**
 * Исключение "Ошибка при получении описания текущего состояния элемента".
 */
public class ElementStateNotFoundException extends RuntimeException {
    public ElementStateNotFoundException(String elementState) {
        super(String.format("Отсутствует описание для состояния \"%s\"", elementState));
    }

    public static Supplier<RuntimeException> get(String stateName){
        return () -> new ElementStateNotFoundException(stateName);
    }
}
