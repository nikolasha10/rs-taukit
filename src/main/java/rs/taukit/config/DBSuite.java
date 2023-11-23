package rs.taukit.config;

import rs.taukit.exceptions.DBConfigNotFoundException;
import rs.taukit.exceptions.DBSuiteConfigNotFoundException;
import rs.taukit.utils.YamlParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record DBSuite(String name, Map<String, DBConfig> dbMap) {
    public DBSuite(String filePath, String suiteName) {
        this(suiteName, new HashMap<>());
        Map<String, ?> dbSuiteConfigs = YamlParser.parse(filePath);
        Object configData = Optional.ofNullable(dbSuiteConfigs.get(suiteName))
                .orElseThrow(() -> new DBSuiteConfigNotFoundException(suiteName));
        Map<String, ?> dbSuiteConfig = YamlParser.convert(configData);
        Map<String, Map<String, String>> dbConfigs = YamlParser.convert(dbSuiteConfig.get("databases"));
        String commonType = dbSuiteConfig.get("commonType").toString();
        String commonName = dbSuiteConfig.get("commonName").toString();
        String commonUser = dbSuiteConfig.get("commonUser").toString();
        String commonPassword = dbSuiteConfig.get("commonPassword").toString();
        dbConfigs.forEach((name, options) -> {
            String dbType = options.getOrDefault("type", commonType);
            String dbHost = options.getOrDefault("host", Config.getSTAND().getHost());
            String dbName = options.getOrDefault("name", commonName);
            String dbPort = options.get("port");
            String dbUser = options.getOrDefault("user", commonUser);
            String dbPassword = options.getOrDefault("password", commonPassword);
            String dbScheme = options.get("scheme");
            dbMap.put(name, new DBConfig(dbType, dbHost, dbPort, dbName, dbScheme, dbUser, dbPassword));
        });
    }

    public DBConfig get(String dbName) {
        return Optional.ofNullable(dbMap.get(dbName)).orElseThrow(() -> new DBConfigNotFoundException(dbName));
    }
}
