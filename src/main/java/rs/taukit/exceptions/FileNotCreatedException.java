package rs.taukit.exceptions;

/**
 * Исключение "Не удалось создать файл".
 */
public class FileNotCreatedException extends RuntimeException {

    public FileNotCreatedException(Throwable cause, String filePath) {
        super(String.format("Файл '%s' не был создан", filePath), cause);
    }

}
