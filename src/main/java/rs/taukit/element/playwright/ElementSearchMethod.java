package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.interfaces.IElement;

import java.util.Arrays;
import java.util.function.Function;

import static rs.taukit.step.utils.BasicUtils.driver;

public enum ElementSearchMethod {
    BY_XPATH ("xpath", (element) -> {
        PlaywrightDriver driver = element.getDriver().castTo();
        return driver.getPage().locator(element.getLocator());
    }),
    BY_TEST_ID ("testId", (element) -> {
        PlaywrightDriver driver = element.getDriver().castTo();
        return driver.getPage().getByTestId(element.getLocator());
    }),
    BY_LABEL ("label", (element) -> {
        PlaywrightDriver driver = element.getDriver().castTo();
        return driver.getPage().getByLabel(element.getLocator());
    }),
    BY_ROLE ("role", (element) -> {
        PlaywrightDriver driver = element.getDriver().castTo();
        return driver.getPage().getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(element.getLocator()));
    });

    private final String methodName;
    private final Function<IElement, Locator> method;

    ElementSearchMethod(String methodName, Function<IElement, Locator> method) {
        this.methodName = methodName;
        this.method = method;
    }

    public static Function<IElement, Locator> searchBy(String methodName){
        return Arrays.stream(values()).filter(method -> method.methodName.equalsIgnoreCase(methodName))
                .findFirst().orElseThrow().method;
    }

}
