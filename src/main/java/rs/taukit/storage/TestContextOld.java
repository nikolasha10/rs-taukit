//package rs.taukit.storage;
//
//import com.microsoft.playwright.Browser;
//import com.microsoft.playwright.BrowserContext;
//import com.microsoft.playwright.Page;
//import com.microsoft.playwright.Playwright;
//import io.cucumber.testng.Pickle;
//import rs.taukit.testData.Employee;
//import rs.taukit.testData.TestData;
//import rs.taukit.page.playwright.PlaywrightPageObject;
//import rs.taukit.page.PageStorage;
//
//public class TestContextOld {
//    private final ThreadLocal<Playwright> playwrightLocal = new ThreadLocal<>();
//    private final ThreadLocal<Browser> browserLocal = new ThreadLocal<>();
//    private final ThreadLocal<BrowserContext> browserContextLocal = new ThreadLocal<>();
//    private final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
//    private final ThreadLocal<String> currentPageNameLocal = new ThreadLocal<>();
//    private final ThreadLocal<String> savedPageNameLocal = new ThreadLocal<>();
//    private final ThreadLocal<Employee> currentUserLocal = new ThreadLocal<>();
//    private final ThreadLocal<String> currentScenarioNameLocal = new ThreadLocal<>();
//    private final ThreadLocal<String> currentFunctionalityLocal = new ThreadLocal<>();
//    private final ThreadLocal<Pickle> currentPickleLocal = new ThreadLocal<>();
//    private final ThreadLocal<TestData> testDataLocal = new ThreadLocal<>();
//
//    private static final class UserContextHolder{
//        private static final TestContext INSTANCE = new TestContext();
//    }
//    public static TestContext getInstance(){
//        return UserContextHolder.INSTANCE;
//    }
//
//    public void setPlaywright(Playwright playwright){
//        playwrightLocal.set(playwright);
//    }
//    public void setBrowser(Browser browser){
//        browserLocal.set(browser);
//    }
//    public void setContext(BrowserContext context){
//        browserContextLocal.set(context);
//    }
//    public void setCurrentPageName(String pageName){
//        currentPageNameLocal.set(pageName);
//    }
//    public void saveCurrentPageName(){
//        savedPageNameLocal.set(currentPageNameLocal.get());
//    }
//    public void setPage(Page page){
//        pageThreadLocal.set(page);
//    }
//    public void setCurrentUser(Employee user){
//        currentUserLocal.set(user);
//    }
//    public void setCurrentPickle(Pickle pickle) {
//        currentPickleLocal.set(pickle);
//    }
//    public void setCurrentScenarioName(String scenarioName){
//        currentScenarioNameLocal.set(scenarioName);
//    }
//    public void setCurrentFunctionality(String functionality){
//        currentFunctionalityLocal.set(functionality);
//    }
//    public void setTestData(TestData testData){
//        testDataLocal.set(testData);
//    }
//
//    public Playwright getPlaywright(){
//        return playwrightLocal.get();
//    }
//    public Browser getBrowser(){
//        return browserLocal.get();
//    }
//    public BrowserContext getContext(){
//        return browserContextLocal.get();
//    }
//    public Page getPage(){
//        return pageThreadLocal.get();
//    }
//    public String getCurrentPageName(){
//        return currentPageNameLocal.get();
//    }
//    public PlaywrightPageObject getCurrentPage(){
//        return PageStorage.getInstance().getPage(getCurrentPageName());
//    }
//    public String getSavedPageName(){
//        return currentPageNameLocal.get();
//    }
//    public Employee getCurrentUser(){
//        return currentUserLocal.get();
//    }
//    public Pickle getCurrentPickle(){ return currentPickleLocal.get(); }
//    public String getCurrentScenarioName(){
//        return currentScenarioNameLocal.get();
//    }
//    public String getCurrentFunctionality(){
//        return currentFunctionalityLocal.get();
//    }
//    public TestData getTestData() {
//        return testDataLocal.get();
//    }
//
//}
