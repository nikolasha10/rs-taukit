package rs.taukit.config;

import lombok.Getter;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.storage;

/**
 * Класс настроек фреймворка
 */
public class Config {
    private static final Properties CONFIG_PROPERTIES = new Properties();
    private static final Properties RUNTIME_PROPERTIES = new Properties();
    @Getter
    private static Stand STAND;
    @Getter
    private static DBSuite DBSUITE;
    private static Map<String, String> pageMapping;
    @Getter
    private static Map<String, String> fragmentMapping;
    @Getter
    private static Dependencies dependencies;

    public static void init(String configDirPath) {
        setConfig(CONFIG_PROPERTIES, configDirPath + "/taukit.properties", true);
        setConfig(RUNTIME_PROPERTIES, configDirPath + "/runtime.properties", false);
        STAND = new Stand(configDirPath + "/stands.yaml", getProperty("standName"));
        DBSUITE = new DBSuite(configDirPath + "/databases.yaml", STAND.getDbSuite());
        pageMapping = buildPageMapping(getProperty("pagesDirPath"));
        fragmentMapping = buildPageMapping(getProperty("pageFragmentsDirPath"));
        dependencies = new Dependencies(configDirPath + "/dependencies.yaml");
        LinkedHashMap<String, ?> standDataMap = new LinkedHashMap<>(STAND.getStandData());
        storage.put("Тестовый стенд", new TestData("StandData", standDataMap));
        String testDataIndexPath = getProperty("testDataIndexPath");
        if (testDataIndexPath != null) {
            LinkedHashMap<String, ?> testDataMap = YamlParser.parse(testDataIndexPath);
            storage.put("ТД", new TestData("TestData", testDataMap));
        }
    }

    private static void setConfig(Properties properties, String filePath, boolean required) {
        try {
            InputStream configInputStream = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(configInputStream, StandardCharsets.UTF_8);
            properties.load(reader);
            if (!required) {
                TauLogger.console("[примечание] установлены значения параметров из файла '%s'", filePath);
            }
        } catch (IOException e) {
            if (required) {
                throw new ConfigFileNotFoundException(e, filePath);
            }
        }
    }

    private static Map<String, String> buildPageMapping(String pagesDirPath) {
        Set<File> pageFileList = FileUtils.getDirFileList(pagesDirPath);
        return pageFileList.stream()
                .filter(file -> !file.getName().endsWith(".md"))
                .collect(Collectors.toMap(file -> {
                    Map<String, ?> pageData = YamlParser.parse(file.getAbsolutePath());
                    return pageData.get("Название").toString();
                }, File::getAbsolutePath));
    }

    public static String eval(String template) {
        String format = "\\$\\{(.+?)}";
        Matcher matcher = Pattern.compile(format).matcher(template);
        try {
            if (matcher.matches() && Objects.requireNonNull(getProperty(matcher.group(1))).startsWith("$")) {
                return getProperty(matcher.group(1));
            }
            return matcher.replaceAll(matchResult -> getProperty(matchResult.group(1)));
        } catch (IllegalArgumentException e) {
            String message = String.format("Не удалось вычислить значение свойства '%s'", template);
            throw new TestDataException(e, message);
        }
    }

    public static String getProperty(String propertyName) {
        try {
            return eval(Optional.ofNullable(Optional.ofNullable(System.getProperty(propertyName))
                            .orElse(RUNTIME_PROPERTIES.getProperty(propertyName)))
                    .orElse(CONFIG_PROPERTIES.getProperty(propertyName)));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Boolean getBooleanProperty(String propertyName) {
        return Boolean.parseBoolean(getProperty(propertyName));
    }

    public static int getIntegerProperty(String propertyName) {
        return Integer.parseInt(Objects.requireNonNull(getProperty(propertyName)));
    }

    public static double getTimeProperty(String propertyName) {
        return TimeStringParser.getTime(Objects.requireNonNull(getProperty(propertyName)));
    }

    public static String getPageFilePath(String pageName) {
        return pageMapping.get(pageName);
    }

    public static String getFragmentFilePath(String blockName) {
        return fragmentMapping.get(blockName);
    }
}