package rs.taukit.step;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.cucumber.datatable.DataTable;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.TestNGCucumberRunner;
import io.qameta.allure.Step;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.step.utils.BasicUtils;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.testdata.FormData;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.testdata.TestDataInstruction;
import rs.taukit.utils.PdfUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
public class BasicStory extends BasicUtils {
    @Step("подготовить тестовые параметры")
    public static void testParameterInit() {
        Pickle pickle = testContext().getPickle();
        ScenarioData scenarioData = new ScenarioData(pickle);
        scenarioData.getAttachments().forEach(TauLogger::attach);
        testContext().setScenarioData(scenarioData);
    }

    @Step("открыть браузер")
    public static void openBrowser() {
        if (!driver().isBrowserOpen()) {
            DriverManager.getInstance().getDriver().openBrowser();
        }
        TauLogger.report("Открыт браузер '%s'", driver().getBrowserType());
    }

    @Step("заполнить поля формы значениями:")
    public static void fillFormFields(DataTable dataTable) {
        TauLogger.attachDataTable("Значения полей формы", dataTable);
        FormData formData = new FormData(dataTable, page());
        formData.fillForm();
    }

    @Step("открыть страницу '{pageName}'")
    public static void openPage(String pageName) {
        pageStorage().getPage(pageName).loadPage();
        TauLogger.report("Открыта страница '%s'", pageName);
    }

    public static void setValue(String fieldName, String value) {
        Class<IElement> elementType = ElementType.getType("поле").getElementClass();
        page().replaceElement(fieldName, elementType).castTo(IHasEditableValue.class).setValue(value);
    }

    @Step("нажать на элемент '{elementName}'")
    public static void clickElement(String elementName) {
        element(elementName).click();
        TauLogger.report("Нажали на элемент '%s'", elementName);
    }

    @Step("нажать на кнопку '{buttonName}'")
    public static void clickButton(String buttonName) {
        page().waitForElement(buttonName, true);
        testContext().setStepActionPageName(testContext().getCurrentPageName());
        element(buttonName).click();
        testContext().setStepAction(() -> {
            element(buttonName).click();
            return true;
        });

    }

    @Step("дождаться отображения страницы '{pageName}'")
    public static void waitForPageLoad(String pageName) {
        pageStorage().getPage(pageName).waitForLoad();
        TauLogger.report("Отображается страница '%s'", pageName);
    }

    @Step("дождаться отображения всплывающего окна '{pageName}'")
    public static void waitForPopupWindow(String pageName) {
        pageStorage().getPopup(pageName).waitForLoad();
        TauLogger.report("Отображается всплывающее окно '%s'", pageName);
    }

    @Step("сделать скриншот")
    public static void takeScreenshot() {
        TauLogger.attachScreenshot(page().takeScreenshot());
    }

    @Step("в списке '{listName}' выбрать значение '{value}'")
    public static void selectListValue(String listName, String value) {
        element(listName).castTo(IHasEditableValue.class).setValue(value);
        TauLogger.report("В списке '%s' выбран элемент '%s'", listName, value);
    }

    public static void waitForTimeout(String timeValue, String timeUnit) {
        page().waitForTimeout(TimeStringParser.getTime(timeValue + " " + timeUnit));
    }

    @Step("дождаться выполнения условий:")
    public static void expectedResultCheck(DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        dataTable.asMaps().forEach(data -> new ExpectedResult(data, page()).waitForResult());
    }

    @Step("сравнить печатную форму с эталоном")
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

    public static void createPdfTemplate(String templateName, String instruction) {
        TestDataInstruction testDataInstruction = scenarioData().get(instruction);
        createPdfTemplate(templateName, testDataInstruction.getDataTable());
    }
    public static void createPdfTemplate(String templateName, DataTable dataTable) {
        try {
            Files.createDirectories(new File("target/templates").toPath());
            File fileTemplate = new File("target/templates", templateName);
            if (fileTemplate.exists()) Files.delete(fileTemplate.toPath());
            Files.createFile(fileTemplate.toPath());
            dataTable.asMaps().forEach(item -> {
                String templateType = TestDataUtils.get(item, "Директория");
                String fragmentName = TestDataUtils.get(item, "Фрагмент");
                File fragment = new File(String.format("src/test/efr/resources/templates/%s/%s", templateType, fragmentName));
                try {
                    Files.writeString(fileTemplate.toPath(), Files.readString(fragment.toPath()), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createPdfStandard(String standardFileName, String originFileName, String templateName, DataTable dataTable) {
        File template = new File("target/templates", templateName);
        String templateContent = null;
        try {
            templateContent = Files.readString(template.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dataTable.asMaps().forEach(item -> {
            String parameterName = TestDataUtils.get(item, "Параметр");
            String parameterValue = TestDataUtils.get(item, "Значение");
            scenarioData().put(parameterName, parameterValue);
        });
        String evaluatingContent = eval(templateContent);
        File evaluatingTemplate = new File("target/templates", templateName.replace(".txt", " eval.txt"));
        try {
            if (evaluatingTemplate.exists()) {
                Files.delete(evaluatingTemplate.toPath());
            }
            Files.writeString(evaluatingTemplate.toPath(), evaluatingContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String originFilePath = String.format("target/downloads/%s/%s",
                testContext().getScenarioName(), originFileName);
        String editedFilePath = String.format("target/downloads/%s/%s",
                testContext().getScenarioName(), standardFileName);
        PdfUtils.replacePdfContent(originFilePath, editedFilePath, evaluatingContent);
        TauLogger.attachFile("Тест", "html", "d:\\autotest\\efr\\Изображения\\1.html");
    }

    @Step("сравнить файл '{originFileName}' с эталоном")
    public static void comparePdfWithStandard(String originFileName, String standardFileName) {
        String originFilePath = String.format("target/downloads/%s/%s",
                testContext().getScenarioName(), originFileName);
        String standardFilePath = String.format("target/downloads/%s/%s",
                testContext().getScenarioName(), standardFileName);
        PlaywrightDriver driver = driver().castTo();
        driver.openBrowser(true);
        Page pageOriginal = driver.getPage();
        page().waitForTimeout(2000);
        String originalPdfUrl = new File(originFilePath).toURI().toString();
        pageOriginal.navigate(originalPdfUrl);
        pageOriginal.waitForCondition(() -> pageOriginal.locator("//div[@class='page' and @data-loaded='true'][1]").isVisible(),
                new Page.WaitForConditionOptions().setTimeout(60000));
        Locator scaleSelect = pageOriginal.locator("//select[@id='scaleSelect']");
        if (!scaleSelect.inputValue().equals("page-actual")) {
            scaleSelect.click();
            scaleSelect.selectOption("Реальный размер");
        }
        Locator pageNumberOriginal = pageOriginal.locator("//input[@id='pageNumber']");
        int pageCountOriginal = Integer.parseInt(pageNumberOriginal.getAttribute("Max"));

        driver.openNewTab();
        String standardPdfUrl = new File(standardFilePath).toURI().toString();
        Page pageStandard = driver.getPage();
        pageStandard.navigate(standardPdfUrl);
        pageStandard.waitForCondition(() -> pageStandard.locator("//div[@class='page' and @data-loaded='true'][1]").isVisible(),
                new Page.WaitForConditionOptions().setTimeout(60000));
        Locator scaleSelectStandard = pageStandard.locator("//select[@id='scaleSelect']");
        if (!scaleSelectStandard.inputValue().equals("page-actual")) {
            scaleSelectStandard.click();
            scaleSelectStandard.selectOption("Реальный размер");
        }
        Locator pageNumberStandard = pageStandard.locator("//input[@id='pageNumber']");
        int pageCountStandard = Integer.parseInt(pageNumberStandard.getAttribute("Max"));
        int pageCount = Integer.max(pageCountOriginal, pageCountStandard);
        for (int i = 1; i <= pageCount; i++) {
            String xpath = String.format("//div[@class='page' and @data-page-number='%d' and @data-loaded='true']/div[1]", i);
            String xpath1 = String.format("//div[@class='page' and @data-page-number='%d' and @data-loaded='true']", i);

            byte[] originalScreenshotData = null;
            byte[] standardScreenshotData = null;
            int pageOriginalWidth = 0;
            int pageOriginalHeight = 0;
            int pageStandardWidth = 0;
            int pageStandardHeight = 0;
            if (i <= pageCountOriginal) {
                pageOriginal.bringToFront();
                pageNumberOriginal.fill(String.valueOf(i));
                pageNumberOriginal.blur();
                Locator pageOriginalLocator = pageOriginal.locator(xpath1);
                pageOriginalLocator.isVisible();
                pageOriginalWidth = (int) pageOriginal.evaluate("element => element.offsetWidth", pageOriginal.locator(xpath1).elementHandle());
                pageOriginalHeight = 20 + (int) pageOriginal.evaluate("element => element.offsetHeight", pageOriginal.locator(xpath1).elementHandle());
                pageOriginal.setViewportSize(1280, pageOriginalHeight);
                pageOriginalLocator.isVisible();
                originalScreenshotData = pageOriginal.locator(xpath).screenshot();
            }

            if (i <= pageCountStandard){
                pageStandard.bringToFront();
                pageNumberStandard.fill(String.valueOf(i));
                pageNumberStandard.blur();
                Locator pageStandardLocator = pageStandard.locator(xpath1);
                pageStandardLocator.isVisible();
                pageStandardWidth = (int) pageStandard.evaluate("element => element.offsetWidth", pageStandard.locator(xpath1).elementHandle());
                pageStandardHeight = 20 + (int) pageStandard.evaluate("element => element.offsetHeight", pageStandard.locator(xpath1).elementHandle());

                pageStandard.setViewportSize(1280, pageStandardHeight);
                standardScreenshotData = pageStandard.locator(xpath).screenshot();
            }

            try {
                BufferedImage originalImage = originalScreenshotData == null
                        ? new BufferedImage(Integer.max(pageOriginalWidth, pageStandardWidth),
                        Integer.max(pageOriginalHeight, pageStandardHeight), BufferedImage.TYPE_INT_BGR)
                        : ImageIO.read(new ByteArrayInputStream(originalScreenshotData));
                Screenshot originalScreenshot = new Screenshot(originalImage);
                BufferedImage standardImage = standardScreenshotData == null
                        ? new BufferedImage(Integer.max(pageOriginalWidth, pageStandardWidth),
                        Integer.max(pageOriginalHeight, pageStandardHeight), BufferedImage.TYPE_INT_BGR)
                        : ImageIO.read(new ByteArrayInputStream(standardScreenshotData));
                Screenshot standardScreenshot = new Screenshot(standardImage);
                ImageDiff diff = new ImageDiffer().makeDiff(originalScreenshot, standardScreenshot);
                if (diff.hasDiff()) {
                    BufferedImage diffImage = diff.getMarkedImage();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(diffImage, "png", byteArrayOutputStream);
                    byteArrayOutputStream.close();
                    ByteArrayOutputStream originalBytes = new ByteArrayOutputStream();
                    ImageIO.write(originalImage, "png", originalBytes);
                    originalBytes.close();
                    ByteArrayOutputStream standardBytes = new ByteArrayOutputStream();
                    ImageIO.write(standardImage, "png", standardBytes);
                    standardBytes.close();
                    TauLogger.warn(() -> {
                        TauLogger.attachScreenshot("Отличия", byteArrayOutputStream.toByteArray());
                        TauLogger.attachScreenshot("Фактический результат", originalBytes.toByteArray());
                        TauLogger.attachScreenshot("Ожидаемый результат", standardBytes.toByteArray());
                    }, "страница %d отличается от ожидаемой", i);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        driver.switchBrowser(0);
    }

    @Step("сохранить значения поля '{elementName}', как '{parameterName}'")
    public static void saveElementValue(String elementName, String parameterName) {
        scenarioData().put(parameterName, element(elementName).getText());
    }
}