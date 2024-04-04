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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.driver;
import static rs.taukit.utils.BasicUtils.scenarioData;

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
        attachFullScreenshots();
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
        attachFullScreenshots();
    }

    public static void warn(Throwable e, String format, Object... args) {
        warn(String.format(format, args), e);
    }

    /**
     * Выводит сообщение о фатальной ошибке в действии, и делает скриншот.
     */
    @Step("{0}")
    public static void fail(String message) {
//        attachPageSource();
        attachFullScreenshots();
        throw new AssertionError(message);
    }

    /**
     * Выводит сообщение о фатальной ошибке в действии. так же выводит сообщение об ошибке
     */
    @Step("{message}")
    public static void fail(String message, Throwable e) {
//        attachPageSource();
        attachFullScreenshots();
        throw new AssertionError(message, e);
    }

    @Step("{message}")
    public static void fail(Runnable runnable, String message) {
        runnable.run();
        throw new AssertionError(message);
    }

    public static void fail(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        fail(message, e);
    }

    public static void fail(String format, Object... args) {
        fail(String.format(format, args));
    }

    public static void fail(Runnable runnable, String format, Object... args) {
        String message = String.format(format, args);
        if (scenarioData().getOptional("NEXT_CHECK_NOT_CRITICAL") == null) {
            fail(runnable, message);
        } else {
            warn(runnable, format, args);
            scenarioData().put("NEXT_CHECK_NOT_CRITICAL", null);
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
        return imageData;
    }

    public static void attachFullScreenshots() {
        List<byte[]> screenshotList = driver().getCurrentBrowser().takeScreenshots();
        IntStream.range(0, screenshotList.size())
                .forEach(index -> attachScreenshot(
                        String.format("Вкладка %d", index + 1),
                        screenshotList.get(index)));
    }

    //
//    public static void attachFullScreenshot() {
//        try {
//            int height = Integer.parseInt(DriverManager.execute("return window.innerHeight").toString());
//            DriverManager.execute("window.scrollTo(0,0)");
//            int currentScrollBy = Integer.parseInt(DriverManager.execute("return window.pageYOffset").toString());
//            int lastScrollBy = -1;
//            while (currentScrollBy - lastScrollBy > 0) {
//                attachScreenshot();
//                lastScrollBy = currentScrollBy;
//                DriverManager.execute("window.scrollBy(0,arguments[0])", height);
//                currentScrollBy = Integer.parseInt(DriverManager.execute("return window.pageYOffset").toString());
//            }
//        } catch (Exception e){
//            CustomLogger.debug("Не удалось сделать снимок экрана");
//        }
//    }
//
//    /**
//     * Создает Page source страницы.
//     */
//    @Attachment(value = "Page source", type = "text/xml")
//    public static byte[] attachPageSource() {
//        try {
//            return DriverManager.get().getPageSource().getBytes();
//        } catch (Exception e) {
//            return new byte[0];
//        }
//    }
//
//    public static byte[] attachData(List<Map<String, String>> parameters) {
//        try {
//            String headers = String.join("\t", parameters.get(0).keySet()) + "\r\n";
//            String text = headers + parameters.stream()
//                    .map(item -> String.join("\t", item.values()))
//                    .collect(Collectors.joining("\r\n"));
//            return text.getBytes();
//        } catch (Exception e) {
//            return new byte[0];
//        }
//    }
//
//    public static byte[] attachData(Map<String, String> parameterMap) {
//        try {
//            String headers = String.join("\t", "Параметр", "Значение") + "\r\n";
//            String text = headers + parameterMap.entrySet().stream()
//                    .map(entry -> String.join("\t", entry.getKey(), entry.getValue()))
//                    .collect(Collectors.joining("\r\n"));
//            return text.getBytes();
//        } catch (Exception e) {
//            return new byte[0];
//        }
//    }
//
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

    //
//    @Attachment(value = "Таблица параметров", type = "text/tab-separated-values", fileExtension = ".csv")
//    public static byte[] attachDataTable(List<Map<String, String>> parameters) {
//        return attachData(parameters);
//    }
//
//    @Attachment(value = "Таблица параметров", type = "text/tab-separated-values", fileExtension = ".csv")
//    public static byte[] attachDataTable(Map<String, String> parameterMap) {
//        return attachData(parameterMap);
//    }
//
//    @Attachment(value = "{tableName}", type = "text/tab-separated-values", fileExtension = ".csv")
//    public static byte[] attachDataTable(String tableName, Map<String, String> parameterMap) {
//        return attachData(parameterMap);
//    }
//
//    @Attachment(value = "{tableName}", type = "text/tab-separated-values", fileExtension = ".csv")
//    public static byte[] attachDataTable(String tableName, List<Map<String, String>> parameterList) {
//        return attachData(parameterList);
//    }
//
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
}
