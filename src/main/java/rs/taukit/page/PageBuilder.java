package rs.taukit.page;

import com.microsoft.playwright.Page;
import rs.taukit.config.Config;
import rs.taukit.exceptions.PageFileNotFoundException;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Map<String, ?> pageConfig = loadPageConfig(pageName);
        String pageUrl = pageConfig.get("Адрес").toString();
        String pageControlUrl = pageConfig.get("Обязательная часть адреса").toString();
        Map<String, String> pageElements = YamlParser.convert(pageConfig.get("Элементы"));
        List<String> expectedElements = YamlParser.convert(pageConfig.get("Обязательные элементы"));
        double pageTimeout = TimeStringParser.getTime(pageConfig.get("Максимальное время ожидания").toString());
        Page.WaitForConditionOptions options = new Page.WaitForConditionOptions().setTimeout(pageTimeout);
        Page.ScreenshotOptions screenshotOptions = new Page.ScreenshotOptions()
                .setPath(Paths.get(pageName + ".png"))
                .setFullPage(true);
        return new PageObject(pageName, pageUrl, pageControlUrl, pageElements, expectedElements, options, screenshotOptions);
    }
}
