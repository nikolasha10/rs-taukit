package rs.taukit.storage;

import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.driver.DriverManager;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.step.allure.BasicApplication;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class ScenarioStorage {
    private final DriverManager driverManager = new DriverManager();
    private final HashMap<String, BasicApplication> application = new HashMap<>();
    private final List<Runnable> stepActions = new ArrayList<>();
    private Pickle pickle;
    private String scenarioName;
    private String functionality;
    @Setter
    private ScenarioData scenarioData;
    @Setter
    private TestData currentUser;
    @Setter
    private String currentPageName;
    private String currentContextName;
    @Setter
    private String savedPageName;
    @Setter
    private IHasBlockElements savedContext;
    @Setter
    private String stepActionPageName;
    private boolean saveActions = false;
    @Setter
    private long freeSince;

    public void update(PickleWrapper pickleWrapper, String functionality) {
        this.pickle = pickleWrapper.getPickle();
        this.scenarioName = pickle.getName();
        this.functionality = functionality;

    }

    public void saveStepAction(Runnable stepAction) {
        if (saveActions) {
            this.stepActions.add(stepAction);
        }
    }

    public void clearStepActions() {
        this.stepActions.clear();
    }

    public BasicApplication getApplication(String appName) {
        return application.get(appName);
    }

    public void putApplication(String appName, BasicApplication app) {
        application.putIfAbsent(appName, app);
    }

    public void startToSaveActions() {
        this.saveActions = true;
    }

    public void stopToSaveActions() {
        this.saveActions = false;
    }

    public void setCurrentContextName(String format, Object... args) {
        currentContextName = String.format(format, args);
    }

}
