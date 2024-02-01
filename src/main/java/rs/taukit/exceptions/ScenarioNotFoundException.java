package rs.taukit.exceptions;

/**
 * Сценарий \"%s\" не найден".
 */
public class ScenarioNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     * @param cause исключение
     * @param scenarioName название сценария
     */
    public ScenarioNotFoundException(Throwable cause, String scenarioName) {
        super(String.format("Сценарий \"%s\" не найден", scenarioName), cause);
    }
    public ScenarioNotFoundException(String scenarioName) {
        super(String.format("Сценарий \"%s\" не найден", scenarioName));
    }
}
