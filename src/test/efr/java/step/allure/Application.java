package step.allure;

import rs.taukit.config.Config;
import rs.taukit.step.allure.BasicApplication;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static rs.taukit.utils.BasicUtils.*;

public class Application extends BasicApplication {
    String appName;
    TestData appData;
    String startPageName;
    SubScenario subScenario = SubScenario.getInstance();

    public Application(String appName) {
        this.appName = appName;
        LinkedHashMap<String, Object> appDataParameters = new LinkedHashMap<>();
        appDataParameters.put("тип", "тестовые данные");
        appDataParameters.put("файл", Config.getProperty("applicationsFilePath"));
        appDataParameters.put("поиск по полям", Map.of("название", appName));
        this.appData = new TestData(appName, appDataParameters);
        this.startPageName = appData.get("стартовая страница");
    }

    public boolean isUserLoggedIn(User user) {
        return driver().isBrowserOpen()
                && browser().getUserLogin() != null
                && user.getLogin().equals(browser().getUserLogin());
    }

    public void login(String userDescription) {
        User user = new User(userDescription);
        Application activeApp = (Application) scenarioStorage().getApplication(appName);
        Application app = Optional.ofNullable(activeApp).orElse(new Application(appName));
        if (!app.isUserLoggedIn(user)) {
            app.login(user);
            scenarioStorage().putApplication(appName, app);
        }
    }

    public void login(User user) {
        Map<String, ?> startPageData = TestDataUtils.loadPageConfig(startPageName);
        String relativeUrl = YamlParser.convert(startPageData.get("Адрес"));
        String url = Config.getSTAND().getUrl() + relativeUrl;
        scenarioData().put("Адрес стартовой страницы", url);
        scenarioData().put("Пользователь", user.getUserData());
        subScenario.runScenario(null, appData.get("сценарий входа"));
        browser().setUserLogin(user.getLogin());
    }
}
