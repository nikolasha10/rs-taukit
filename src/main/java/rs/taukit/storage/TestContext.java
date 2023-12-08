package rs.taukit.storage;

import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageStorage;
import rs.taukit.step.allure.BasicApplication;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestData;

import java.util.HashMap;
import java.util.function.BooleanSupplier;

@Getter
public class TestContext {
    private final HashMap<String, BasicApplication> application = new HashMap<>();
    private Pickle pickle;
    private String scenarioName;
    private String functionality;
    @Setter
    private ScenarioData scenarioData;
    @Setter
    private TestData currentUser;
    @Setter
    private String currentPageName;
    @Setter
    private String savedPageName;
    @Setter
    private String stepActionPageName;
    private BooleanSupplier stepAction;
    @Setter
    private TestNGCucumberRunner runner;


    public TestContext(PickleWrapper pickleWrapper, String functionality) {
        this.pickle = pickleWrapper.getPickle();
        this.scenarioName = pickle.getName();
        this.functionality = functionality;
    }

    public void update(PickleWrapper pickleWrapper, String functionality) {
        this.pickle = pickleWrapper.getPickle();
        this.scenarioName = pickle.getName();
        this.functionality = functionality;

    }

    public void setStepAction(BooleanSupplier stepAction) {
        this.stepAction = () -> {
            PageObject pageObject = PageStorage.getInstance().getPage(stepActionPageName);
            try {
                pageObject.waitForLoad();
                return stepAction.getAsBoolean();
            } catch (Exception e) {
                return false;
            }
        };
    }

    public void clearStepAction() {
        this.stepAction = () -> true;
    }

    public BasicApplication getApplication(String appName) {
        return application.get(appName);
    }

    public void putApplication(String appName, BasicApplication app) {
        application.putIfAbsent(appName, app);
    }
}
