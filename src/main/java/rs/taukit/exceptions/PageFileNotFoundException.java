package rs.taukit.exceptions;

/**
 * Исключение "Не удалось найти или обработать файл страницы".
 */
public class PageFileNotFoundException extends AutoTestBrokenException {

    /**
     * Конструктор.
     * @param name название страницы
     */
    public PageFileNotFoundException(String name) {
        super("Страница с именем '%s' не найдена", name);
    }

    /**
     * Конструктор.
     * @param name название страницы
     * @param cause исключение
     */
    public PageFileNotFoundException(Throwable cause, String name) {
        super(cause, "Не удалось найти или обработать файл страницы '%s'", name);
    }
}
