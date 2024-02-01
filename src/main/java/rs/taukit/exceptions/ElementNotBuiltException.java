package rs.taukit.exceptions;

/**
 * Исключение "Ошибка при создании элемента".
 */
public class ElementNotBuiltException extends RuntimeException {
    public ElementNotBuiltException(Throwable cause, String elementName, String className, String structure) {
        super(String.format("Ошибка при создании элемента \"%s\" класса \"%s\" по данным %s. " +
                        "Возможно, элемент не описан в yaml-файле текущей страницы " +
                        "или описание имеет неправильную структуру, " +
                        "или его название совпадает с названием другого элемента, найденного в текущем контексте " +
                        "и имеющего другой тип и структуру полей",
                elementName, className, structure), cause);
    }

}
