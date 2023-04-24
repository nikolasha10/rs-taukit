package rs.taukit.exceptions;

/**
 * Исключение "Не удалось найти или обработать файл".
 */
public class ConfigFileNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     * @param filePath название страницы
     */
    public ConfigFileNotFoundException(String filePath) {
        super(String.format("Не удалось найти или обработать файл '%s'", filePath));
    }

    /**
     * Конструктор.
     * @param filePath название страницы
     * @param cause исключение
     */
    public ConfigFileNotFoundException(Throwable cause, String filePath) {
        super(String.format("Не удалось найти или обработать файл '%s'", filePath), cause);
    }
}
