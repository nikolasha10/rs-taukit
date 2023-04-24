package rs.taukit.context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.testng.Pickle;
import rs.taukit.config.Employee;
import rs.taukit.config.TestData;
import rs.taukit.page.PageObject;
import rs.taukit.page.PageStorage;

public class TestContext {
    private final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<BrowserContext> browserContextThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<String> currentPageNameThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<Employee> currentUserThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<String> currentScenarioNameLocal = new ThreadLocal<>();
    private final ThreadLocal<String> currentFunctionalityLocal = new ThreadLocal<>();
    private final ThreadLocal<Pickle> currentPickleLocal = new ThreadLocal<>();
    private final ThreadLocal<TestData> testDataLocal = new ThreadLocal<>();

    private static final class UserContextHolder{
        private static final TestContext INSTANCE = new TestContext();
    }
    public static TestContext getInstance(){
        return UserContextHolder.INSTANCE;
    }

    public void setPlaywright(Playwright playwright){
        playwrightThreadLocal.set(playwright);
    }
    public void setBrowser(Browser browser){
        browserThreadLocal.set(browser);
    }
    public void setContext(BrowserContext context){
        browserContextThreadLocal.set(context);
    }
    public void setCurrentPageName(String pageName){
        currentPageNameThreadLocal.set(pageName);
    }
    public void setPage(Page page){
        pageThreadLocal.set(page);
    }
    public void setCurrentUser(Employee user){
        currentUserThreadLocal.set(user);
    }
    public void setCurrentPickle(Pickle pickle) {
        currentPickleLocal.set(pickle);
    }
    public void setCurrentScenarioName(String scenarioName){
        currentScenarioNameLocal.set(scenarioName);
    }
    public void setCurrentFunctionality(String functionality){
        currentFunctionalityLocal.set(functionality);
    }
    public void setTestData(TestData testData){
        testDataLocal.set(testData);
    }

    public Playwright getPlaywright(){
        return playwrightThreadLocal.get();
    }
    public Browser getBrowser(){
        return browserThreadLocal.get();
    }
    public BrowserContext getContext(){
        return browserContextThreadLocal.get();
    }
    public Page getPage(){
        return pageThreadLocal.get();
    }
    public String getCurrentPageName(){
        return currentPageNameThreadLocal.get();
    }
    public PageObject getCurrentPage(){
        return PageStorage.getInstance().getPage(getCurrentPageName());
    }
    public Employee getCurrentUser(){
        return currentUserThreadLocal.get();
    }
    public Pickle getCurrentPickle(){ return currentPickleLocal.get(); }
    public String getCurrentScenarioName(){
        return currentScenarioNameLocal.get();
    }
    public String getCurrentFunctionality(){
        return currentFunctionalityLocal.get();
    }
    public TestData getTestData() {
        return testDataLocal.get();
    }

}
