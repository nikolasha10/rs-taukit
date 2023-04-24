package rs.taukit.exceptions;

/**
 * Исключение "Не удалось найти или обработать файл страницы".
 */
public class PageFileNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     * @param name название страницы
     */
    public PageFileNotFoundException(String name) {
        super(String.format("Страница с именем '%s' не найдена", name));
    }

    /**
     * Конструктор.
     * @param name название страницы
     * @param cause исключение
     */
    public PageFileNotFoundException(Throwable cause, String name) {
        super(String.format("Не удалось найти или обработать файл страницы '%s'", name), cause);
    }
}
