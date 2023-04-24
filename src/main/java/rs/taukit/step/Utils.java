package rs.taukit.step;

import com.microsoft.playwright.Browser;
import io.cucumber.datatable.DataTable;
import rs.taukit.config.Employee;
import rs.taukit.config.TestData;
import rs.taukit.context.TestContext;
import rs.taukit.page.PageObject;

import java.util.stream.Collectors;

public class Utils {
    public static TestContext getContext(){
        return TestContext.getInstance();
    }
    public static Browser getBrowser() {
        return getContext().getBrowser();
    }

    public static PageObject getPage() {
        return getContext().getCurrentPage();
    }

    public static Employee getCurrentUser() {
        return getContext().getCurrentUser();
    }

    public static String eval(String template) {
        TestData testData = getContext().getTestData();
        String evalResult = testData.evalObjects(template);
        return testData.evalStrings(evalResult);
    }
    public static DataTable eval(DataTable dataTable) {
        TestData testData = getContext().getTestData();
        return DataTable.create(dataTable.asLists().stream()
                .map(dataRow -> dataRow.stream()
                        .map(testData::evalStrings)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList()));
    }
}
