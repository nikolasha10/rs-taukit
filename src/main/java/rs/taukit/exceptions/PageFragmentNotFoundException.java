package rs.taukit.exceptions;

import rs.taukit.config.Config;

/**
 * Исключение "Не удалось найти общий блок элементов".
 */
public class PageFragmentNotFoundException extends AutoTestBrokenException {

    /**
     * Конструктор.
     * @param name название общего блока
     */
    public PageFragmentNotFoundException(String name) {
        super("Общий блок элементов с названием \"%s\" не найден. Список общих блоков: %s",
                name, Config.getFragmentMapping());
    }

    /**
     * Конструктор.
     * @param name название общего блока
     * @param cause исключение
     */
    public PageFragmentNotFoundException(Throwable cause, String name) {
        super(cause, "Не удалось найти или обработать файл страницы \"%s\"", name);
    }
}
