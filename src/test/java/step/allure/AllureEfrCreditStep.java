package step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.playwright.PwBlock;
import rs.taukit.element.playwright.PwList;
import rs.taukit.element.playwright.PwTable;
import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class AllureEfrCreditStep extends AllureBasicStep {
    @Step("дождаться отображения действия '{actionName}' на вкладке 'История'")
    public void waitForActionOnHistoryTab(String actionName) {
        AtomicReference<Exception> exception = new AtomicReference<>();
        BooleanSupplier actions = () -> {
            page().reload();
            driver().waitForCondition(() -> {
                element("Меню вкладок страницы").castTo(PwList.class).setValue("История");
                PwBlock historyBlock = element("История").castTo(PwBlock.class);
                return historyBlock.isVisible();
            });
            try {
                PwBlock historyBlock = element("История").castTo(PwBlock.class);
                PwTable historyTable = historyBlock.getElementByName("История").castTo(PwTable.class);
                driver().waitForCondition(historyTable::isVisible);
                List<List<String>> checkDataList = Arrays.asList(
                        Arrays.asList("Столбец", "Значение"),
                        Arrays.asList("ДЕЙСТВИЕ", "Предварительный расчет лимита"));
                DataTable dataTable = DataTable.create(checkDataList, TextUtils.tableConverter);
                historyTable.findRowByConditions(dataTable);
                return true;
            } catch (Exception e) {
                exception.set(e);
                page().waitForTimeout(3000);
                return false;
            }
        };
        try {
            driver().waitForCondition(actions);
            TauLogger.report("На вкладке 'История' отобразилось действие \"%s\"", actionName);
        } catch (Exception e) {
            if (exception.get() != null) e.addSuppressed(exception.get());
            TauLogger.fail(e, "Не удалось дождаться отображения действия \"%s\" на вкладке 'История'", actionName);
        }
    }
}
