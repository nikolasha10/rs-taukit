package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import rs.taukit.config.Config;
import rs.taukit.exceptions.ConfigFileNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

/**
 * Класс отвечающий за вывод сообщений в лог и allure-отчеты
 */
public final class TauLogger {
    private static final Logger logger = LoggerFactory.getLogger(Config.getProperty("logger.name"));

    @Step("{0}")
    public static void report(String message) {
        logger.info(message.replaceAll("\n", "↵"));
    }

    public static void report(String format, Object... args) {
        String message = String.format(format, args);
        report(message);
    }

    /**
     * Выводит информационное сообщение только в лог, но не в Allure отчет
     *
     * @param message
     */
    public static void console(String message) {
        logger.info(message);
    }

    public static void console(String format, Object... args) {
        String message = String.format(format, args);
        console(message);
    }

    /**
     * Собирает содержимое файлов с логами по каждому сценарию, и выводит их в стандартный вывод
     */
    public static void collectLogs(){
        if (!logger.getName().equalsIgnoreCase("scenarioLog")) return;
        Logger consoleLogger = LoggerFactory.getLogger("collectLogs");
        String logPath = String.format("%s.log", MDC.get("logName"));
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
//
//    /**
//     * Выводит сообщение об ошибке в действии и делает скриншот.
//     */
//    @Step("Некритическая ошибка: {message}")
//    public static void warn(String message) {
//        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"));
//        Storage.put("WARNING", "PASSED with ERRORS");
//        attachPageSource();
//        attachFullScreenshot();
//    }
//
//    public static void warn(String format, Object... args) {
//        warn(String.format(format, args));
//    }
//
//    /**
//     * Выводит сообщение об ошибке в действии и НЕ делает скриншот.
//     */
//    @Step("Некритическая ошибка: {message}")
//    public static void warnWOSS(String message) {
//        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"));
//        Storage.put("WARNING", "PASSED with ERRORS");
//    }
//
//    @Step("Некритическая ошибка: {message}")
//    public static void warnWOSS(String message, Throwable e) {
//        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"), e);
//        Storage.put("WARNING", "PASSED with ERRORS");
//    }
//
//    public static void warnWOSS(String message, Object... args) {
//        warnWOSS(String.format(message, args));
//    }
//
//    public static void warnWOSS(Throwable e, String message, Object... args) {
//        warnWOSS(String.format(message, args), e);
//    }
//
//    /**
//     * Выводит сообщение об ошибке в действии и саму ошибку.
//     */
//    @Step("Некритическая ошибка: {message}")
//    public static void warn(String message, Throwable e) {
//        logger.warn("ОШИБКА: " + message.replaceAll("\n", "↵"), e);
//        Storage.put("WARNING", "PASSED with ERRORS");
//        attachPageSource();
//        attachFullScreenshot();
//    }
//
//    public static void warn(Throwable e, String format, Object... args) {
//        warn(String.format(format, args), e);
//    }
//
//    /**
//     * Выводит сообщение о фатальной ошибке в действии и делает скриншот.
//     */
//    @Step("{0}")
//    public static void fail(String message) {
//        attachPageSource();
//        attachFullScreenshot();
//        throw new AssertionError(message);
//    }
//
//    /**
//     * Выводит сообщение о фатальной ошибке в действии. так же выводит сообщение об ошибке
//     */
//    @Step("{0}")
//    public static void fail(String message, Throwable e) {
//        attachPageSource();
//        attachFullScreenshot();
//        throw new AssertionError(message, e);
//    }
//
//    public static void fail(Throwable e, String format, Object... args) {
//        String message = String.format(format, args);
//        fail(message, e);
//    }
//
//    public static void fail(String format, Object... args) {
//        fail(String.format(format, args));
//    }
//
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
     * @param functionality - значение поля <b>Функционал</b> в сценарии cucumber
     * @param scenarioName - значение поля <b>Сценарий</b>
     */
    public static void setLogPath(String functionality, String scenarioName) {
        String logPath = Config.getProperty("logger.default.logPath");
        String logName = String.format("%s/%s/%s", logPath, functionality, scenarioName);
        MDC.put("logName", logName);
    }

//    /**
//     * Создает скриншот страницы.
//     */
//    @Attachment(value = "Page screenshot", type = "image/jpg")
//    public static byte[] attachScreenshot() {
//        if (MBDriverManager.isCurrent()) {
//            Context currentContext = DriverUtils.getContext();
//            try {
//                if (currentContext == Context.WEB_APP) {
//                    DriverUtils.setNativeContext();
//                }
//                return ((TakesScreenshot) MBDriverManager.get()).getScreenshotAs(OutputType.BYTES);
//            } catch (Exception e) {
//                return new byte[0];
//            } finally {
//                DriverUtils.setContext(currentContext);
//            }
//        } else {
//            WebDriver driver = IKDriverManager.get();
//            try {
//                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
//            } catch (Exception e) {
//                return new byte[0];
//            }
//        }
//    }
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
//
//    @Attachment(value = "{fileName}", fileExtension = "{fileExtension}")
//    public static byte[] attachFile(String fileName, String fileExtension, String filePath) {
//        File file = new File(filePath);
//        try {
//            return Files.readAllBytes(file.toPath());
//        } catch (IOException ioException) {
//            warnWOSS(ioException, "Не удалось приложить файл '%s' к отчету", filePath);
//        }
//        return null;
//    }
}
