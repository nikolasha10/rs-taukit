package rs.taukit.step.allure;

import com.microsoft.playwright.TimeoutError;
import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import io.qameta.allure.Step;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import rs.taukit.driver.WebDriver;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Методы класса реализуют общие шаги сценариев для всех проектов
 * Аннотация @Step позволяет вывести сообщение в Allure-отчет
 */
public class AllureBasicStep extends BasicUtils {
    @Step("определить значения тестовых параметров")
    public static void testParameterInit() {
        Pickle pickle = testContext().getPickle();
        ScenarioData scenarioData = new ScenarioData(pickle);
        scenarioData.getAttachments().forEach(TauLogger::attach);
        testContext().setScenarioData(scenarioData);
    }

    @Step("открыть браузер")
    public static void openBrowser() {
        WebDriver driver = driver();
        if (!driver.isBrowserOpen()) {
            driver.openBrowser();
        }
        TauLogger.report("Открыт браузер '%s'", driver().getBrowserType());
    }

    @Step("нажать на элемент '{elementName}'")
    public static void clickElement(String elementName) {
        element(elementName).click();
        TauLogger.report("Нажали на элемент '%s'", elementName);
    }

    @Step("нажать на поле '{fieldName}'")
    public static void clickTextField(String fieldName) {
        element(fieldName).click();
        TauLogger.report("Нажали на поле '%s'", fieldName);
    }

    @Step("нажать на кнопку '{buttonName}'")
    public static void clickButton(String buttonName) {
        try {
            page().waitForElement(buttonName, true);
        } catch (TimeoutError ignored) {
            TauLogger.console("Не удалось дождаться отображения кнопки '%s'", buttonName);
        }
        testContext().setStepActionPageName(testContext().getCurrentPageName());
        try {
            element(buttonName).click();
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на кнопку '%s'", buttonName);
        }
        testContext().setStepAction(() -> {
            element(buttonName).click();
            return true;
        });
        TauLogger.report("Нажата кнопка '%s'", buttonName);
    }

    @Step("снимок: {screenshotName}")
    public static void takeScreenshot(String screenshotName) {
        TauLogger.attachScreenshot(screenshotName, page().takeScreenshot());
    }

    public static void waitForTimeout(String timeValue, String timeUnit) {
        TauLogger.console("Ждём %s %s", timeValue, timeUnit);
        driver().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
//        page().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
    }

    @Step("дождаться выполнения условий:")
    public static void expectedResultCheck(DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        dataTable.asMaps().forEach(data -> new ExpectedResult(data).waitForResult());
    }

    @Step("сравнить файл \"(.+)\" с эталоном \"(.+)\"")
    public static void validatePrintForm() {
        String printFormName = "Анкета-заявление на предоставление кредита (односторонняя)";
        try {
            Files.createDirectories(new File(String.format("src/test/efr/resources/templates/%s", printFormName)).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(String.format("target/downloads/01.02 Отладка сравнения печатных форм/%s.pdf", printFormName));
        File fileResult = new File(String.format("target/downloads/01.02 Отладка сравнения печатных форм/%s после преобразования.pdf", printFormName));
        File fileTemplate = new File(String.format("src/test/efr/resources/templates/%s.txt", printFormName));
        List<String> templateLineList = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        if (fileTemplate.exists()) {
            try {
                templateLineList.addAll(Files.readAllLines(fileTemplate.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (PDDocument document = PDDocument.load(file)) {
            PDPageTree tree = document.getDocumentCatalog().getPages();
            IntStream.range(0, tree.getCount()).boxed().forEach(pageIndex -> {
                PDPage page = tree.get(pageIndex);
                List<PDStream> pdStreamList = new ArrayList<>();
                page.getContentStreams().forEachRemaining(pdStream -> {
                    File fileTemplatePage = new File(String.format("src/test/efr/resources/templates/%s/page_%d.txt", printFormName, pageIndex + 1));
                    List<String> templatePageLineList = new ArrayList<>();
                    if (fileTemplatePage.exists()) {
                        try {
                            templatePageLineList.addAll(Files.readAllLines(fileTemplatePage.toPath()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        InputStream inputStream = pdStream.createInputStream();
                        OutputStream outputStream = pdStream.createOutputStream();
                        Charset charset = Charset.forName("windows-1251");
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset.newDecoder());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, charset.newEncoder());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String format = "\\((.+)?\\)Tj";
                        List<String> lineList = bufferedReader.lines().toList();
                        IntStream.range(0, lineList.size()).boxed().forEach(i -> {
                            String line = lineList.get(i);
//                            Matcher matcher = Pattern.compile(format).matcher(line);
//                            if (matcher.matches()) {
                            if (templatePageLineList.size() > i) {
//                                    if (matcher.group(1) != null) {
                                line = templateLineList.get(i);
//                                    }
//                                    index.set(index.get()+1);
                            } else {
//                                    templatePageLineList.add(Optional.ofNullable(matcher.group(1)).orElse(""));
                                templatePageLineList.add(line);
                            }
//                            }
                            try {
//                                System.out.println(line);
                                outputStreamWriter.write(line);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        bufferedReader.close();
                        outputStreamWriter.close();
                        inputStreamReader.close();
                        outputStream.close();
                        inputStream.close();
                        pdStreamList.add(pdStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!fileTemplatePage.exists()) {
                        try {
                            Files.write(fileTemplatePage.toPath(), templatePageLineList, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                try {
                    page.setContents(pdStreamList);
                    document.save(fileResult);
                    document.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
//            if (!fileTemplate.exists()) {
//                Files.write(fileTemplate.toPath(), templateLineList, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Step("примечание: далее работаем в контексте блока '{blockName}'")
    public static void setBlockContext(String blockName) {
        page().setCurrentContext(blockName);
        TauLogger.console("Текущий контекст: Блок элементов '%s'", blockName);
    }

    public static void setCurrentPageContext() {
        page().setCurrentContext(null);
        TauLogger.report("Текущий контекст: Страница '%s'", testContext().getCurrentPageName());
    }

    @Step("примечание: переходим к контексту блока '{blockName}'")
    public static void closeCurrentAndSetNewBlockContext(String blockName) {
        closeCurrentContext();
        page().setCurrentContext(blockName);
        TauLogger.report("Текущий контекст: Блок элементов '%s'", blockName);
    }

    public static void closeCurrentContext() {
        IElement currentBlock = page().dropCurrentContext();
        String currentContext = currentBlock == null
                ? String.format("Страница '%s'", testContext().getCurrentPageName())
                : String.format("Блок '%s'", currentBlock.getElementName());
        TauLogger.report("примечание: текущий контекст - %s", currentContext);
    }

    @Step("обновить страницу")
    public static void reloadPage() {
        page().reload();
        TauLogger.report("Страница '%s' обновлена", testContext().getCurrentPageName());
    }

    @Step("закрыть текущую вкладку")
    public static void closeCurrentTab() {
        int tabIndex = page().getCurrentTabIndex();
        driver().closeCurrentTab();
        TauLogger.report("Вкладка %d закрыта", tabIndex + 1);
    }
}