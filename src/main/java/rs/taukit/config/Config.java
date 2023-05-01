package rs.taukit.config;

import lombok.Getter;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.utils.TauLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Read configuration and provide it by methods.
 */
public class Config {
    private static final Properties CONFIG_PROPERTIES = new Properties();
    private static final Properties RUNTIME_PROPERTIES = new Properties();
    @Getter
    private static Stand STAND;

    public static void init(String configDirPath){
        setConfig(CONFIG_PROPERTIES, configDirPath + "/taukit.properties", true);
        setConfig(RUNTIME_PROPERTIES, configDirPath + "/runtime.properties", false);
        STAND = new Stand(configDirPath + "/stands.yaml", getProperty("standName"));
    }
    private static void setConfig(Properties properties, String filePath, boolean required){
        try {
            InputStream configInputStream = new FileInputStream(filePath);
            properties.load(configInputStream);
        } catch (IOException e) {
            if (required) {
                throw new ConfigFileNotFoundException(e, filePath);
            } else {
                TauLogger.console("Не удалось найти или обработать файл '%s'", filePath);
            }
        }
    }
    public static String getProperty(String propertyName){
        return Optional.ofNullable(Optional.ofNullable(System.getProperty(propertyName))
                .orElse(RUNTIME_PROPERTIES.getProperty(propertyName)))
                .orElse(CONFIG_PROPERTIES.getProperty(propertyName));
    }

    public static Boolean getBooleanProperty(String propertyName){
        return Boolean.getBoolean(getProperty(propertyName));
    }

//    private static final int SECOND = 1000;
//    private static Stand currentStand;
//
//
//    static {
//        try {
//
//            InputStream configInputStream = new FileInputStream("src/main/resources/config.properties");
//            CONFIG_PROPERTIES.load(configInputStream);
//        } catch (IOException e) {
//            CustomLogger.failWOSS("Unable to load configuration file", e);
//        }
//        try {
//            InputStream configInputStream = new FileInputStream("src/main/resources/runtime.properties");
//            RUNTIME_PROPERTIES.load(configInputStream);
//        } catch (IOException ignored) {
//            CustomLogger.debug("%s %s",
//                    "Не удалось загрузить параметры текущей конфигурации запуска тестов",
//                    "из файла src/main/resources/runtime.properties");
//        }
//        String standName = getRuntimeProperty("testStand.name");
//        try {
//            currentStand = new Stand(standName);
//            Storage.put("Название тестового стенда", currentStand.getName());
//        } catch (IOException e) {
//            CustomLogger.failWOSS(e, "Не удалось получить настройки стенда '%s'", standName);
//        }
//    }
//
//    private Config() {
//    }
//
//    public static Stand getCurrentStand() {
//        return currentStand;
//    }
//
//    /**
//     * Hub url getter from project configuration.
//     *
//     * @return configured hub url
//     */
//    public static URL getHubUrl() {
//        try {
//            return new URL(getRuntimeProperty("selenium.hub.url"));
//        } catch (MalformedURLException e) {
//            CustomLogger.failWOSS("Hub URL in configuration file is malformed");
//            throw new IllegalStateException(e);
//        }
//    }
//
//    /**
//     * Global (default) timeout getter from project configuration.
//     *
//     * @return configured global timeout
//     */
//    public static int getGlobalTimeout() {
//        return Integer.parseInt(getRuntimeProperty("timeout.global"));
//    }
//
//    public static int getCommandTimeout() {
//        return Integer.parseInt(CONFIG_PROPERTIES.getProperty("timeout.command"));
//    }
//
//    /**
//     * Animation duration getter from project configuration.
//     *
//     * @return configured animation duration
//     */
//    public static int getAnimationTimeout() {
//        return Integer.parseInt(CONFIG_PROPERTIES.getProperty("timeout.animation")) * SECOND;
//    }
//
//    /**
//     * Animation duration getter from project configuration.
//     *
//     * @return configured animation duration
//     */
//    public static int getOneSecTimeout() {
//        return Integer.parseInt(CONFIG_PROPERTIES.getProperty("timeout.sec"));
//    }
//
//    /**
//     * Timeout for loading pages getter from project configuration.
//     *
//     * @return configured timeout fot page loading (in seconds)
//     */
//    public static int getPageLoadingTimeout() {
//        return Integer.parseInt(getRuntimeProperty("timeout.page.load"));
//    }
//
//    /**
//     * Timeout for loading pages getter from project configuration.
//     *
//     * @return configured timeout fot page loading (in seconds)
//     */
//    public static int getAuthenticationTimeout() {
//        return Integer.parseInt(getRuntimeProperty("timeout.authentication"));
//    }
//
//    /**
//     * URL for application location getter from project configuration.
//     *
//     * @param device  application platform
//     * @param version version of application
//     * @return configured URL (string representation)
//     */
//    public static String getAppLocation(Device device, String version) {
//        return getRuntimeProperty("app.location.url");
//    }
//
//    /**
//     * Determines if wait strategy is 'fast' or not.
//     *
//     * @return true if wait.strategy from configuration is 'fast'
//     */
//    public static boolean getWaitStrategy() {
//        return "fast".equals(CONFIG_PROPERTIES.getProperty("wait.strategy"));
//    }
//
//    /**
//     * returns WDB URL from configuration.
//     *
//     * @return URL
//     */
//    public static String getPageUrl(String webUser) {
//        String baseUrl = currentStand.getBaseUrl();
//        switch (webUser) {
//            case "АРМ сотрудника":
//                return baseUrl + currentStand.getEmployeeARM();
//            case "АРМ клиента":
//                return baseUrl + currentStand.getCustomerARM();
//            default:
//                return baseUrl + currentStand.getTestAppIndex();
//        }
//    }
//
//    /**
//     * returns path of pdf from configuration.
//     *
//     * @return URL
//     */
//    public static String getPathPDFLocation() {
//        return CONFIG_PROPERTIES.getProperty("user.dir.pdf");
//    }
//
//    public static String getPathUpload_filesLocation() {
//        return String.format("%s/%s/Common/test_data/upload_files",
//                Config.getRuntimeProperty("project.home"),
//                Config.getRuntimeProperty("project.name"));
//    }
//
//    public static String getUploadFileLocation(String fileName) {
//        String commonUploadFilePath = String.format("%s/%s/Common/upload_files/%s",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"),
//                fileName);
//        String standUploadFilePath = String.format("%s/%s/%s/upload_files/%s",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"),
//                Config.getCurrentStand().getName(),
//                fileName);
//        String temporaryFilePath = String.format("%s/%s",
//                Config.getRuntimeProperty("temporary.files.path"),
//                fileName);
//        if (new File(standUploadFilePath).exists()) {
//            return standUploadFilePath;
//        }
//        if (new File(commonUploadFilePath).exists()) {
//            return commonUploadFilePath;
//        }
//        return temporaryFilePath;
//    }
//
//    public static String getStandardFileLocation(String fileName) {
//        String commonUploadFilePath = String.format("%s/%s/Common/standard_files/%s",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"),
//                fileName);
//        String standUploadFilePath = String.format("%s/%s/%s/standard_files/%s",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"),
//                Config.getCurrentStand().getName(),
//                fileName);
//        if (new File(standUploadFilePath).exists()) {
//            return standUploadFilePath;
//        }
//        return commonUploadFilePath;
//    }
//
//    public static String getSqlFileLocation(String fileName) {
//        return "src/test/resources/sql_scripts/" + fileName;
//    }
//
//    public static Map<String, ?> getUser(String userDescription) {
//        String commonUserListPath = String.format("%s/%s/Common/Пользователи.yaml",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"));
//        String standUserListPath = String.format("%s/%s/%s/Пользователи.yaml",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"),
//                Config.getRuntimeProperty("testStand.name"));
//
//        File standFile = new File(standUserListPath);
//        File commonFile = new File(commonUserListPath);
//        Map<String, ?> user = null;
//        if (standFile.exists()){
//            user = getUser(standFile, userDescription);
//        }
//        if (user == null) {
//            user = Optional.ofNullable(getUser(commonFile, userDescription))
//                    .orElseThrow(() -> new ValueIsWrongException(
//                            "Не удалось найти данные учётной записи клиента по описанию '%s' в файлах '%s' и '%s'",
//                            userDescription, standFile.getAbsolutePath(), commonFile.getAbsolutePath()));
//        }
//        return user;
//    }
//
//    public static Map<String, ?> getUser(File file, String userDescription) {
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        try {
//            Map<String, List<Map<String, ?>>> commonUserList = mapper.readValue(file, new TypeReference<>() {
//            });
//            return commonUserList.get("Клиенты").stream().filter(map -> {
//                List<?> descriptions = (List<?>) map.get("description");
//                return descriptions.stream().anyMatch(item -> item.toString().equals(userDescription));
//            }).findFirst().orElse(null);
//        } catch (IOException ioException){
//            throw new ValueIsWrongException("Не удалось получить данные пользователей", ioException);
//        }
//    }
//
//    public static Map<String, ?> getBankEmployee(String userDescription) {
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        String commonUserListPath = String.format("%s/%s/Common/Пользователи.yaml",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"));
//        String standUserListPath = String.format("%s/%s/%s/Пользователи.yaml",
//                Config.getRuntimeProperty("test_data.path"),
//                Config.getRuntimeProperty("project.name"),
//                Config.getRuntimeProperty("testStand.name"));
//
//        File standFile = new File(standUserListPath);
//        File commonFile = new File(commonUserListPath);
//        Map<String, ?> user = null;
//        if (standFile.exists()){
//            user = getEmployee(standFile, userDescription);
//        }
//        if (user == null) {
//            user = Optional.ofNullable(getEmployee(commonFile, userDescription))
//                    .orElseThrow(() -> new ValueIsWrongException(
//                            "Не удалось найти данные учётной записи сотрудника банка по описанию '%s' в файлах '%s' и '%s'",
//                            userDescription, standFile.getAbsolutePath(), commonFile.getAbsolutePath()));
//        }
//        return user;
//    }
//
//    public static Map<String, ?> getEmployee(File file, String userDescription) {
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        try {
//            Map<String, List<Map<String, ?>>> commonUserList = mapper.readValue(file, new TypeReference<>() {
//            });
//            return commonUserList.get("Сотрудники банка").stream().filter(map -> {
//                List<?> descriptions = (List<?>) map.get("description");
//                return descriptions.stream().anyMatch(item -> item.toString().equals(userDescription));
//            }).findFirst().orElse(null);
//        } catch (IOException ioException){
//            throw new ValueIsWrongException("Не удалось получить данные пользователей", ioException);
//        }
//    }
//    public static String getVersionOldAppForUpdate() {
//        return CONFIG_PROPERTIES.getProperty("old.version.app");
//    }
//
//    /**
//     * returns path of pdf from configuration.
//     *
//     * @return URL
//     */
//    public static String getAppLocationUrl() {
//        return CONFIG_PROPERTIES.getProperty("app.location.url");
//    }
//
//    /**
//     * returns path to WDB driver from configuration.
//     *
//     * @return path to WDB driver
//     */
//    public static String getWebDriverPath() {
//        String webDriverPath = System.getProperty("web.driver.path");
//        return webDriverPath == null ? CONFIG_PROPERTIES.getProperty("web.driver.path") : webDriverPath;
//    }
//
//    /**
//     * Determines if WDB driver is alive or not (from configuration).
//     *
//     * @return true if WDB driver is alive
//     */
//    public static boolean webDriverAlive() {
//        return CONFIG_PROPERTIES.getProperty("web.driver.alive").toLowerCase().equals("true");
//    }
//
//    /**
//     * Determines if wdb.driver.headless property is true or not (from configuration).
//     *
//     * @return property value
//     */
//    public static boolean getWebHeadless() {
//        return CONFIG_PROPERTIES.getProperty("web.driver.headless").toLowerCase().equals("true");
//    }
//
//    /**
//     * returns name of database from configuration.
//     *
//     * @return name of Database for connection
//     */
//    public static String getDatabaseName() {
//        return CONFIG_PROPERTIES.getProperty("abs.database");
//    }
//
//    /**
//     * returns path to image file for upload
//     *
//     * @return path to image file for upload without name file
//     */
//
//    public static String getImagesPathForLoad() {
//        return CONFIG_PROPERTIES.getProperty("path.upload.files");
//    }
//
//    public static String getDownloadPath() {
//        return getRuntimeProperty("browser.download.path");
//    }
//
//    public static String getRuntimeProperty(String propertyName) {
//        return Optional.ofNullable(Optional.ofNullable(System.getProperty(propertyName))
//                .orElse(RUNTIME_PROPERTIES.getProperty(propertyName)))
//                .orElse(CONFIG_PROPERTIES.getProperty(propertyName));
//    }
}