package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import rs.taukit.config.Config;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.TestDataFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.*;

/**
 * Класс отвечающий за вывод сообщений в лог и allure-отчеты
 */
public final class TauLogger {
    private static final Logger logger = LoggerFactory.getLogger(Config.getProperty("logger.name"));

    @Step("{0}")
    public static void report(String message) {
        logger.info(message);
    }

    @Step("{0}")
    public static void report(String message, DataTable dataTable) {
        logger.info(String.format("%s%n%s", message, dataTable));
    }

    public static void report(String format, Object... args) {
        String message;
        if (Arrays.stream(args).toList().getLast() instanceof DataTable dataTable) {
            message = String.format(format, Arrays.stream(args).toList().subList(0, args.length - 1).toArray());
            report(message, dataTable);
        } else {
            message = String.format(format, args);
            report(message);
        }
    }

    /**
     * Выводит информационное сообщение только в лог, но не в Allure отчет
     *
     * @param message сообщение выводимое в лог
     */
    public static void console(String message) {
        logger.info(message);
    }

    public static void console(String format, Object... args) {
        String message;
        if (Arrays.stream(args).toList().getLast() instanceof DataTable dataTable) {
            String realFormat = format + "%n%s";
            args[args.length - 1] = String.valueOf(dataTable);
            message = String.format(realFormat, args);
        } else {
            message = String.format(format, args);
        }
        console(message);
    }

    /**
     * Собирает содержимое файлов с логами по каждому сценарию, и выводит их в стандартный вывод
     */
    public static void collectLogs() {
        if (!logger.getName().equalsIgnoreCase("scenarioLog")) return;
        Logger consoleLogger = LoggerFactory.getLogger("collectLogs");
        String logPath = String.format("%s.log", MDC.get("logName").replaceAll("\"", "_"));
        File file = new File(logPath);
        try {
            consoleLogger.info(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new ConfigFileNotFoundException(e, file.getPath());
        }
    }


//    public static void debug(DataTable dataTable) {
//        System.out.println();
//        System.out.println(TextUtils.correctDatatable(dataTable));
//    }

//    /**
//     * Выводит информационное сообщение о действии в Базе Данных.
//     */
//    @Step("{0}")
//    public static void infoDB(String message, Throwable e) {
//        logger.info(message, e);
//    }

    /**
     * Выводит сообщение об ошибке в действии, и делает скриншот.
     */
    @Step("Некритическая ошибка: {message}")
    public static void warn(String message) {
        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"));
        scenarioData().put("WARNING", "PASSED with ERRORS");
        if (page() != null) attachFullScreenshots();
    }

    @Step("Некритическая ошибка: {message}")
    public static void warn(String message, Runnable runnable) {
        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"));
        scenarioData().put("WARNING", "PASSED with ERRORS");
        runnable.run();
    }

    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }

    public static void warn(Runnable runnable, String format, Object... args) {
        warn(String.format(format, args), runnable);
    }

    /**
     * Выводит сообщение об ошибке в действии, и НЕ делает скриншот.
     */
    @Step("Некритическая ошибка: {message}")
    public static void warnWOSS(String message) {
        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"));
        scenarioData().put("WARNING", "PASSED with ERRORS");
    }

    @Step("Некритическая ошибка: {message}")
    public static void warnWOSS(String message, Throwable e) {
        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"), e);
        scenarioData().put("WARNING", "PASSED with ERRORS");
    }

    public static void warnWOSS(String message, Object... args) {
        warnWOSS(String.format(message, args));
    }

    public static void warnWOSS(Throwable e, String message, Object... args) {
        warnWOSS(String.format(message, args), e);
    }

    /**
     * Выводит сообщение об ошибке в действии и саму ошибку.
     */
    @Step("Некритическая ошибка: {message}")
    public static void warn(String message, Throwable e) {
        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"), e);
        scenarioData().put("WARNING", "PASSED with ERRORS");
        if (page() != null) attachFullScreenshots();
    }

    public static void warn(Throwable e, String format, Object... args) {
        warn(String.format(format, args), e);
    }

    /**
     * Выводит сообщение о критической ошибке, и делает скриншот.
     */
    @Step("{0}")
    public static void fail(String message) {
        if (page() != null) attachFullScreenshots();
        throw new AssertionError(message);
    }

    /**
     * Выводит сообщение о критической ошибке.
     */
    @Step("{message}")
    public static void fail(String message, Throwable e) {
//        attachPageSource();
        if (page() != null) attachFullScreenshots();
        throw new AssertionError(message, e);
    }

    @Step("{message}")
    public static void fail(Runnable runnable, String message) {
        runnable.run();
        if (page() != null) attachFullScreenshots();
        if (scenarioData().getOptional("NEXT_CHECK_NOT_CRITICAL") != null) {
            logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"));
            scenarioData().put("WARNING", "PASSED with ERRORS");
        } else {
            throw new AssertionError(message);
        }
    }

    public static void fail(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        if (scenarioData().getOptional("NEXT_CHECK_NOT_CRITICAL") != null) {
            warn(message, e);
        } else {
            fail(message, e);
        }
    }

    public static void fail(String format, Object... args) {
        if (scenarioData().getOptional("NEXT_CHECK_NOT_CRITICAL") != null) {
            warn(format, args);
        } else {
            fail(String.format(format, args));
        }
    }

    public static void fail(Runnable runnable, String format, Object... args) {
        String message = String.format(format, args);
        if (scenarioData().getOptional("NEXT_CHECK_NOT_CRITICAL") != null) {
            warn(runnable, format, args);
        } else {
            fail(runnable, message);
        }
    }

    /**
     * Выводит сообщение о фатальной ошибке в действии и НЕ делает скриншот
     */
    @Step("{0}")
    public static void failWOSS(String message) {
        throw new AssertionError(message);
    }

    /**
     * Выводит сообщение о фатальной ошибке в действии и НЕ делает скриншот
     */
    @Step("{0}")
    public static void failWOSS(String message, Throwable e) {
        throw new AssertionError(message, e);
    }

    public static void failWOSS(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        failWOSS(message, e);
    }

    public static void failWOSS(String format, Object... args) {
        failWOSS(String.format(format, args));
    }

    /**
     * Выводит сообщение о непредвиденной ошибке (ошибке реализации теста)
     */
    @Step("{0}")
    public static void error(String message) {
        throw new RuntimeException(message);
    }

    @Step("{0}")
    public static void error(String message, Throwable e) {
        throw new RuntimeException(message, e);
    }

    public static void error(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        error(message, e);
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    /**
     * Задаёт значение параметра <b>logName</b> для логгера (см. файл logback.xml),
     * который определяет путь к файлу текущего лога
     *
     * @param functionality - значение поля <b>Функционал</b> в сценарии cucumber
     * @param scenarioName  - значение поля <b>Сценарий</b>
     */
    public static void setLogPath(String functionality, String scenarioName) {
        String logPath = Config.getProperty("logger.default.logPath");
        String logName = String.format("%s/%s/%s", logPath, functionality, scenarioName.replaceAll("\"", "_"));
        MDC.put("logName", logName);
    }

    /**
     * Создает скриншот страницы.
     */
    @Attachment(value = "Page screenshot", type = "image/png")
    public static byte[] attachScreenshot(byte[] imageData) {
        return imageData;
    }

    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attachScreenshot(String name, byte[] imageData) {
        if (page() != null) {
            List<List<String>> data = new ArrayList<>();
            data.add(Arrays.asList("url", page().getCurrentUrl()));
            DataTable dataTable = DataTable.create(data, TextUtils.tableConverter);
            attach("Информация: " + name, dataTable);
        }
        return imageData;
    }

    public static void attachFullScreenshots() {
        if (driver().isBrowserOpen()) {
            List<byte[]> screenshotList = driver().getCurrentBrowser().takeScreenshots();
            IntStream.range(0, screenshotList.size())
                    .forEach(index -> {
                        browser().setActiveTab(index);
                        attachScreenshot(
                                String.format("Снимок: Вкладка %d. %s", index + 1, page().getPageName()),
                                screenshotList.get(index));
                    });
        }
    }


    public static byte[] attachData(DataTable dataTable) {
        try {
            return TextUtils.correctDatatable(dataTable).stream()
                    .map(row -> String.join("\t", row))
                    .collect(Collectors.joining("\r\n")).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Attachment(value = "{tableName}", type = "text/tab-separated-values", fileExtension = ".csv")
    public static byte[] attachDataTable(String tableName, DataTable dataTable) {
        return attachData(dataTable);
    }

    @Attachment(value = "{fileName}")
    public static byte[] attachFile(String fileName, String filePath) {
        File file = new File(filePath);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ioException) {
            warnWOSS(ioException, "Не удалось приложить файл \"%s\" к отчету", filePath);
        }
        return null;
    }

    @Attachment(value = "{fileName}", type = "application/pdf")
    public static byte[] attachPdfFile(String fileName, String filePath) {
        File file = new File(filePath);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ioException) {
            warnWOSS(ioException, "Не удалось приложить файл \"%s\" к отчету", filePath);
        }
        return null;
    }

    @Attachment(value = "{fileName}", type = "application/pdf")
    public static byte[] attachJson(String fileName, String filePath) {
        File file = new File(filePath);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ioException) {
            warnWOSS(ioException, "Не удалось приложить файл \"%s\" к отчету", filePath);
        }
        return null;
    }

    @Attachment(value = "{fileName}")
    public static byte[] attachFile(String fileName, byte[] file) {
        return file;
    }

    public static void attach(String name, Object object) {
        switch (object) {
            case DataTable dataTable -> attachDataTable(name, dataTable);
            case TestDataFile file -> attachFile(name, file.getFilePath());
            case String text -> attachFile(name, text.getBytes());
            case null, default ->
                    throw new TestDataException("Не удалось прикрепить объект \"%s\" к отчёту. Не распознан тип вложения.", name);
        }
    }

    public static void resetNotCritical() {
        scenarioData().put("NEXT_CHECK_NOT_CRITICAL", null);
    }

}
