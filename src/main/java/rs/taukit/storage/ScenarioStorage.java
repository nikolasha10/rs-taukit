package rs.taukit.storage;

import com.microsoft.playwright.ConsoleMessage;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.driver.DriverManager;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.step.manager.ApplicationManager;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

@Getter
public class ScenarioStorage {
    private final DriverManager driverManager = new DriverManager();
    private final HashMap<String, ApplicationManager> application = new HashMap<>();
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
    private IContainer savedContext;
    @Setter
    private String stepActionPageName;
    private boolean saveActions = false;
    @Setter
    private long freeSince;
    private final Stack<String> stepStack = new Stack<>();
    @Getter
    private List<String> consoleMessages = new ArrayList<>();

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

    public ApplicationManager getApplication(String appName) {
        return application.get(appName);
    }

    public void putApplication(String appName, ApplicationManager app) {
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

    public void pushStep(String scenarioName) {
        stepStack.push(scenarioName);
    }

    public void popStep() {
        stepStack.pop();
    }

    public String getStepStack() {
        return String.join(" -> ", stepStack);
    }

    public void addConsoleMessage(String message) {
        consoleMessages.add(message);
    }

}
