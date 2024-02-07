package rs.taukit.config;

import lombok.Getter;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

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
    }

    private static void setConfig(Properties properties, String filePath, boolean required) {
        try {
            InputStream configInputStream = new FileInputStream(filePath);
            properties.load(configInputStream);
            if (!required){
                TauLogger.console("[примечание] установлены значения параметров из файла \"%s\"", filePath);
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

    public static String getProperty(String propertyName) {
        return Optional.ofNullable(Optional.ofNullable(System.getProperty(propertyName))
                        .orElse(RUNTIME_PROPERTIES.getProperty(propertyName)))
                .orElse(CONFIG_PROPERTIES.getProperty(propertyName));
    }

    public static Boolean getBooleanProperty(String propertyName) {
        return Boolean.parseBoolean(getProperty(propertyName));
    }

    public static int getIntegerProperty(String propertyName) {
        return Integer.parseInt(getProperty(propertyName));
    }

    public static double getTimeProperty(String propertyName) {
        return TimeStringParser.getTime(getProperty(propertyName));
    }

    public static String getPageFilePath(String pageName) {
        return pageMapping.get(pageName);
    }

    public static String getFragmentFilePath(String blockName) {
        return fragmentMapping.get(blockName);
    }
}