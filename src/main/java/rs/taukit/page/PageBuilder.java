package rs.taukit.page;

import rs.taukit.config.Config;
import rs.taukit.driver.DriverManager;
import rs.taukit.exceptions.PageFileNotFoundException;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.util.List;
import java.util.Map;

public class PageBuilder {
    private static Map<String, ?> loadPageConfig(String pageName) {
        try {
            String pagesDirPath = Config.getProperty("pagesDirPath");
            String pageMappingFilePath = pagesDirPath + "/pageMapping.yaml";
            Map<String, String> pageMapping = YamlParser.parse(pageMappingFilePath);
            String pageFilePath = pagesDirPath + "/" + pageMapping.get(pageName);
            return YamlParser.parse(pageFilePath);
        } catch (Exception e) {
            throw new PageFileNotFoundException(e, pageName);
        }
    }

    public static PageObject buildPage(String pageName) {
        PageObject page = DriverManager.getInstance().getDriver().initPage(pageName);
        Map<String, ?> pageConfig = loadPageConfig(pageName);
        String pageUrl = pageConfig.get("Адрес").toString();
        String pageControlUrl = pageConfig.get("Обязательная часть адреса").toString();
        Map<String, String> pageElements = YamlParser.convert(pageConfig.get("Элементы"));
        List<String> expectedElements = YamlParser.convert(pageConfig.get("Обязательные элементы"));
        String pageTimeoutString = pageConfig.get("Максимальное время ожидания").toString();
        double pageTimeout = TimeStringParser.getTime(pageTimeoutString.equalsIgnoreCase("по умолчанию")
                ? Config.getProperty("pageLoadingTimeout") : pageTimeoutString);
        return page;
    }
}
