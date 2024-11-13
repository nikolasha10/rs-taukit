package rs.taukit.step.manager;

import rs.taukit.driver.DriverManager;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.page.PageObject;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.storage.Storage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.TauLogger;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public abstract class StepManager {
    final Storage globalStorage = Storage.getInstance();
    final CucumberScenario scenario = globalStorage.getScenario();
    protected final ScenarioStorage scenarioStorage = scenario.getScenarioStorage();
    protected final ScenarioData scenarioData = scenarioStorage.getScenarioData();
    final DriverManager driverManager = scenarioStorage.getDriverManager();

    /**
     * Возвращает объект - экземпляр класса драйвера, используемый в данный момент
     *
     * @return текущий драйвер
     */
    protected IWebDriver getDriver() {
        return driverManager.getCurrentDriver();
    }

    /**
     * Возвращает объект экземпляр класса браузера, активный на данный момент
     *
     * @return текущий браузер
     */
    protected IWebBrowser getBrowser() {
        return getDriver().getCurrentBrowser();
    }

    /**
     * Возвращает объект - экземпляр класса страницы, активный на данный момент
     *
     * @return текущая страница
     */
    protected PageObject getPage() {
        return getBrowser().getCurrentPage();
    }

    /**
     * Возвращает объект - экземпляр класса контейнера, в контексте которого работаем в данный момент
     *
     * @return текущий контекст
     */
    IContainer getContext() {
        return getPage().getCurrentContext();
    }

    /**
     * Возвращает элемент, приведенный к типу переменной, которой он присваивается
     *
     * @param elementName - название элемента
     * @return - элемент указанного типа
     */
    @SuppressWarnings("unchecked")
    protected <T> T getElement(String elementName) {
        return getPage().getElement(elementName);
    }

    private boolean skipped() {
        Boolean skipActions = scenarioData.getOptional("SKIP_ACTIONS");
        return Optional.ofNullable(skipActions).orElse(false);
    }

    /**
     * Проверяет, выполнены ли условия для пропуска действий. Если нет, выполняет их.
     * Сохраняет действия для повтора до выполнения условий.
     *
     * @param action - выполняемые действия
     */
    void run(Runnable action) {
        Runnable runnable = () -> {
            if (skipped()) return;
            action.run();
        };
        runnable.run();
        scenarioStorage.saveStepAction(runnable);
    }

    /**
     * Проверяет, выполнены ли условия для пропуска действий. Если нет, выполняет их.
     * Сохраняет действия для повторения до выполнения условий.
     * В случае исключения, завершает тест с сообщением переданного формата.
     *
     * @param action - действия
     * @param format - формат сообщения
     * @param args   - подставляемые аргументы
     */
    void run(Runnable action, String format, Object... args) {
        Runnable runnable = () -> {
            if (skipped()) return;
            try {
                action.run();
            } catch (Exception e) {
                TauLogger.fail(e, format, args);
            }
        };
        runnable.run();
        scenarioStorage.saveStepAction(runnable);
    }

    /**
     * Проверяет, выполнены ли условия для пропуска действий. Если нет, выполняет их.
     * Сохраняет действия для повторения до выполнения условий.
     * В случае исключения, завершает тест с сообщением переданного формата.
     *
     * @param action    - действия
     * @param exception - исключение, возникшее при ожидании выполнения условий. Будет добавлено в трассировку.
     * @param format    - формат сообщения
     * @param args      - подставляемые аргументы
     */

    void run(Runnable action, Exception exception, String format, Object... args) {
        Runnable runnable = () -> {
            if (skipped()) return;
            try {
                action.run();
            } catch (Exception e) {
                if (exception != null) e.addSuppressed(exception);
                TauLogger.fail(e, format, args);
            }
        };
        runnable.run();
        scenarioStorage.saveStepAction(runnable);
    }

    /**
     * Проверяет, выполнены ли условия для пропуска действий. Если нет, выполняет их.
     * В случае исключения, завершает тест с сообщением переданного формата.
     *
     * @param action    - действия
     * @param exception - исключение, возникшее при ожидании выполнения условий. Будет добавлено в трассировку.
     * @param format    - формат сообщения
     * @param args      - подставляемые аргументы
     */
    void runOnce(Runnable action, Exception exception, String format, Object... args) {
        Runnable runnable = () -> {
            if (skipped()) return;
            try {
                action.run();
            } catch (Exception e) {
                if (exception != null) e.addSuppressed(exception);
                TauLogger.fail(e, format, args);
            }
        };
        runnable.run();
    }

    protected void waitForCondition(BooleanSupplier condition, String format, Object... args) {
        run(() -> getDriver().waitForCondition(condition), format, args);
    }

    protected void waitForCondition(BooleanSupplier condition, Exception exception, String format, Object... args) {
        run(() -> getDriver().waitForCondition(condition), exception, format, args);
    }
}
