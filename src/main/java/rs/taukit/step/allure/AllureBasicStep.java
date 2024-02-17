package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.playwright.PwList;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

/**
 * Методы класса реализуют общие шаги сценариев для всех проектов
 * Аннотация @Step позволяет вывести сообщение в Allure-отчет
 */
public class AllureBasicStep extends BasicUtils {
    @Step("определить значения тестовых параметров")
    public static void testParameterInit() {
        scenarioStorage().getScenarioData().getAttachments().forEach(TauLogger::attach);
    }

    @Step("нажать на {elementType} '{elementName}'")
    public static void clickElement(String elementType, String elementName) {
        try {
            element(elementName).click();
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на кнопку \"%s\"", elementName);
        }
        scenarioStorage().saveStepAction(() -> element(elementName).click());
        TauLogger.report("Нажали на %s \"%s\"", elementType, elementName);
    }

    @Step("снимок: {screenshotName}")
    public static void takeScreenshot(String screenshotName) {
        TauLogger.attachScreenshot(screenshotName, page().takeScreenshot());
    }

    public static void waitForTimeout(String timeValue, String timeUnit) {
        TauLogger.console("Ждём %s %s", timeValue, timeUnit);
        driver().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
    }

    @Step("дождаться выполнения условий \"{conditionsName}\":")
    public static void waitForConditions(String conditionsName, DataTable dataTable) {
        waitForConditions(dataTable);
    }

    @Step("дождаться выполнения условий:")
    public static void waitForConditionsNoName(DataTable dataTable) {
        waitForConditions(dataTable);
    }

    public static void waitForConditions(DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        TauLogger.console("Ожидается выполнение условий", dataTable);
        BooleanSupplier conditions = () -> dataTable.asMaps().stream()
                .map(data -> new ExpectedResult(data).waitForResult())
                .allMatch(ExpectedResult::isConditionMet);
        double elementCheckInterval = Config.getTimeProperty("element.check.interval");
        double elementCheckTimeout = Config.getTimeProperty("element.check.timeout");
        try {
            driver().waitForCondition(conditions, elementCheckInterval, elementCheckTimeout, scenarioStorage().getStepActions());
        } catch (Exception e) {
            if (e instanceof AutoTestBrokenException) throw e;
            ExpectedResult firstUnexpectedResult = dataTable.asMaps().stream()
                    .map(data -> new ExpectedResult(data).waitForResult())
                    .filter(result -> !result.isConditionMet())
                    .findFirst().orElse(null);
            if (firstUnexpectedResult != null) {
                if (firstUnexpectedResult.getThrownException() != null) {
                    e.addSuppressed(firstUnexpectedResult.getThrownException());
                }
                String objectType = firstUnexpectedResult.getObjectType();
                String objectName = firstUnexpectedResult.getObjectName();
                String objectState = firstUnexpectedResult.getObjectState();
                String expectedValue = firstUnexpectedResult.getExpectedValue() == null
                        ? ""
                        : String.format(" \"%s\"", firstUnexpectedResult.getExpectedValue());
                TauLogger.fail(e, "Не удалось дождаться выполнения условия: %s \"%s\" %s %s",
                        objectType, objectName, objectState, expectedValue);
            }
        }
        TauLogger.report("Условия выполнены");
        scenarioStorage().clearStepActions();
        scenarioStorage().stopToSaveActions();
    }

    @Step("сравнить файл \"(.+)\" с эталоном \"(.+)\"")
    public static void validatePrintForm() {
        String printFormName = "Анкета-заявление на предоставление кредита (односторонняя)";
        try {
            Files.createDirectories(new File(String.format("src/test/resources/templates/%s", printFormName)).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(String.format("target/downloads/01.02 Отладка сравнения печатных форм/%s.pdf", printFormName));
        File fileResult = new File(String.format("target/downloads/01.02 Отладка сравнения печатных форм/%s после преобразования.pdf", printFormName));
        File fileTemplate = new File(String.format("src/test/resources/templates/%s.txt", printFormName));
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
                    File fileTemplatePage = new File(String.format("src/test/resources/templates/%s/page_%d.txt", printFormName, pageIndex + 1));
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

    @Step("[примечание] текущий контекст: блок '{blockName}'")
    public static void setBlockContext(String blockName) {
        String[] blocks = blockName.split(" -> ");
        Arrays.stream(blocks).forEach(block -> page().setCurrentContext(block));
        scenarioStorage().setCurrentContextName("блок \"%s\"", blockName);
        TauLogger.console("[примечание] текущий контекст: %s", scenarioStorage().getCurrentContextName());
    }

    public static void setCurrentPageContext() {
        page().setCurrentContext(null);
        scenarioStorage().saveStepAction(() -> page().setCurrentContext(null));
        scenarioStorage().setCurrentContextName("страница \"%s\"", scenarioStorage().getCurrentPageName());
        TauLogger.report("[примечание] текущий контекст: страница \"%s\"", scenarioStorage().getCurrentPageName());
    }

    @Step("[примечание] текущий контекст: блок '{blockName}'")
    public static void closeCurrentAndSetNewBlockContext(String blockName) {
        closeCurrentContext(false);
        page().setCurrentContext(blockName);
        TauLogger.console("[примечание] текущий контекст: блок элементов \"%s\"", blockName);
    }

    public static void closeCurrentContext(boolean haveToReport) {
        IElement currentBlock = page().dropCurrentContext();
        String currentContext = currentBlock == null
                ? String.format("страница \"%s\"", scenarioStorage().getCurrentPageName())
                : String.format("блок \"%s\"", currentBlock.getElementName());
        scenarioStorage().setCurrentContextName(currentContext);
        if (haveToReport) {
            TauLogger.report("* текущий контекст: %s", currentContext);
        }
    }

    @Step("дождаться отображения {elementType} '{elementName}'")
    public static void waitForElement(String elementType, String elementName) {
        try {
            page().waitForElement(elementName, true);
            scenarioStorage().saveStepAction(() -> page().waitForElement(elementName, true));
            TauLogger.report("Дождались отображения %s \"%s\"", elementType, elementName);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться отображения %s \"%s\" на странице \"%s\"",
                    elementType, elementName, scenarioStorage().getCurrentPageName());
        }
    }

    @Step("в списке '{listName}' выбрать '{listValue}'")
    public static void listValueClick(String listName, String listValue) {
        PwList pwList = element(listName).castTo(PwList.class);
        pwList.setValue(eval(listValue));
        scenarioStorage().saveStepAction(() -> pwList.setValue(eval(listValue)));
        TauLogger.report("в списке \"%s\" выбран \"%s\"", listName, listValue);
    }
}