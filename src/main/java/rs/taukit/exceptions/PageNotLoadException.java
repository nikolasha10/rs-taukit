package rs.taukit.exceptions;

import com.microsoft.playwright.Page;
import rs.taukit.context.TestContext;
import rs.taukit.page.PageObject;
import rs.taukit.utils.TauLogger;

import java.util.List;

/**
 * Исключение "Не удалось дождаться отображения страницы".
 */
public class PageNotLoadException extends RuntimeException {
    public PageNotLoadException(Throwable cause, boolean isUrlCorrect, List<String> notFoundElements) {
        TestContext testContext = TestContext.getInstance();
        Page page = testContext.getPage();
        String currentUrl = page.url();
        PageObject currentPage = testContext.getCurrentPage();
        String urlNotContainsExpected = isUrlCorrect ? "" : String.format(
                ". Текущий url (%s) не содержит ожидаемого текста (%s)",
                currentUrl, currentPage.getPageControlUrl());
        String elementsNotFound = notFoundElements.size() == 0 ? "" : String.format(
                ". Не найдены ожидаемые элементы: %s", String.join(", ", notFoundElements));
        TauLogger.failWOSS(cause, "Не удалось дождаться отображения страницы '%s'%s%s",
                currentPage.getPageName(), urlNotContainsExpected, elementsNotFound);
    }
}
