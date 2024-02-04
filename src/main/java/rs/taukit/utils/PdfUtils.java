package rs.taukit.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.page.playwright.PlaywrightPageObject;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.*;

public class PdfUtils {
    public static void createPdfStandard(String standardFileName, String originFileName, String templateName, DataTable dataTable) {
        File template = new File("target/templates", templateName);
        String templateContent;
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
                scenario().getName(), originFileName);
        String editedFilePath = String.format("target/downloads/%s/%s",
                scenario().getName(), standardFileName);
        PdfUtils.replacePdfContent(originFilePath, editedFilePath, evaluatingContent);
    }

    @Step("сравнить файл '{testedFileName}' с эталоном")
    public static void comparePdfFiles(String testedFileName, String standardFileName) {
        String testedFilePath = String.format("target/downloads/%s/%s",
                scenario().getName(), testedFileName);
        String standardFilePath = String.format("target/downloads/%s/%s",
                scenario().getName(), standardFileName);
        PlaywrightDriver driver = driver().castTo();
        driver.openBrowser(true);
        PlaywrightPageObject pageObject = driver.getCurrentBrowser().getCurrentPage();
        Page pageTested = pageObject.getPage();
        page().waitForTimeout(2000);
        String originalPdfUrl = new File(testedFilePath).toURI().toString();
        pageTested.navigate(originalPdfUrl);
        pageTested.waitForCondition(() -> pageTested.locator("//div[@class='page' and @data-loaded='true'][1]").isVisible(),
                new Page.WaitForConditionOptions().setTimeout(60000));
        Locator scaleSelect = pageTested.locator("//select[@id='scaleSelect']");
        if (!scaleSelect.inputValue().equals("page-actual")) {
            scaleSelect.click();
            scaleSelect.selectOption("Реальный размер");
        }
        Locator pageNumberOriginal = pageTested.locator("//input[@id='pageNumber']");
        int pageCountOriginal = Integer.parseInt(pageNumberOriginal.getAttribute("Max"));

        driver.getCurrentBrowser().openNewTab();
        String standardPdfUrl = new File(standardFilePath).toURI().toString();
        Page pageStandard = driver.getCurrentBrowser().getCurrentPage().getPage();
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
                pageTested.bringToFront();
                pageNumberOriginal.fill(String.valueOf(i));
                pageNumberOriginal.blur();
                Locator pageOriginalLocator = pageTested.locator(xpath1);
                pageOriginalLocator.isVisible();
                pageOriginalWidth = (int) pageTested.evaluate("element => element.offsetWidth", pageTested.locator(xpath1).elementHandle());
                pageOriginalHeight = 20 + (int) pageTested.evaluate("element => element.offsetHeight", pageTested.locator(xpath1).elementHandle());
                pageTested.setViewportSize(1280, pageOriginalHeight);
                pageOriginalLocator.isVisible();
                originalScreenshotData = pageTested.locator(xpath).screenshot();
            }

            if (i <= pageCountStandard) {
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
                BufferedImage originalImage;
                if (originalScreenshotData == null) {
                    int width = Integer.max(pageOriginalWidth, pageStandardWidth);
                    int height = Integer.max(pageOriginalHeight, pageStandardHeight);
                    originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
                    Graphics2D ig2 = originalImage.createGraphics();
                    ig2.setBackground(Color.WHITE);
                    ig2.clearRect(0, 0, width, height);
                } else {
                    originalImage = ImageIO.read(new ByteArrayInputStream(originalScreenshotData));
                }
                Screenshot originalScreenshot = new Screenshot(originalImage);

                BufferedImage standardImage;
                if (standardScreenshotData == null) {
                    int width = Integer.max(pageOriginalWidth, pageStandardWidth);
                    int height = Integer.max(pageOriginalHeight, pageStandardHeight);
                    standardImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
                    Graphics2D ig2 = originalImage.createGraphics();
                    ig2.setBackground(Color.WHITE);
                    ig2.clearRect(0, 0, width, height);
                } else {
                    standardImage = ImageIO.read(new ByteArrayInputStream(standardScreenshotData));
                }

                Screenshot standardScreenshot = new Screenshot(standardImage);
                ImageDiff diff = new ImageDiffer().makeDiff(originalScreenshot, standardScreenshot);
                if (diff.hasDiff()) {
                    BufferedImage diffImage = diff.getMarkedImage();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(diffImage, "png", byteArrayOutputStream);
                    byteArrayOutputStream.close();
                    scenarioData().put("differences", "data:image/png;base64," + Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));

                    ByteArrayOutputStream testedBytes = new ByteArrayOutputStream();
                    ImageIO.write(originalImage, "png", testedBytes);
                    testedBytes.close();
                    scenarioData().put("tested", "data:image/png;base64," + Base64.getEncoder().encodeToString(testedBytes.toByteArray()));

                    ByteArrayOutputStream standardBytes = new ByteArrayOutputStream();
                    ImageIO.write(standardImage, "png", standardBytes);
                    standardBytes.close();
                    scenarioData().put("expected", "data:image/png;base64," + Base64.getEncoder().encodeToString(standardBytes.toByteArray()));

                    File htmlTemplate = new File("src/test/resources/templates/htmlTemplate.html");
                    String attachment = eval(Files.readString(htmlTemplate.toPath()));
                    TauLogger.warn(() -> TauLogger.attachFile("Различия", ".html", attachment.getBytes()), "страница %d отличается от ожидаемой", i);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        driver.switchBrowser(0);
    }

    public static void replacePdfContent(String originFileName, String editedFileName, String templateContent) {
        File originFile = new File(originFileName);
        File editedFile = new File(editedFileName);
        List<String> templateLineList = templateContent.lines().toList();
        AtomicInteger lineIndex = new AtomicInteger(0);
        try (PDDocument document = PDDocument.load(originFile)) {
            PDPageTree tree = document.getDocumentCatalog().getPages();
            IntStream.range(0, tree.getCount()).boxed().forEach(pageIndex -> {
                PDPage page = tree.get(pageIndex);
                List<PDStream> pdStreamList = new ArrayList<>();
                TauLogger.console("Страница: %d", pageIndex);
                COSDictionary cosDictionary = page.getResources().getCOSObject();
                cosDictionary.entrySet().forEach(entry -> writeToFile(0, entry));
//                try {
//                    System.out.println(page.getResources().getFont(COSName.getPDFName("F3")).getCOSObject().keySet());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                page.getResources().getXObjectNames().forEach(cosName -> TauLogger.console("XObject: %s", cosName));
//                page.getResources().getPropertiesNames().forEach(cosName -> TauLogger.console("Propertie: %s", cosName));
//                page.getResources().getPatternNames().forEach(cosName -> TauLogger.console("Pattern: %s", cosName));
//                page.getResources().getColorSpaceNames().forEach(cosName -> {
//                    try {
//                        TauLogger
//                                .console("ColorSpace: %s - %d", cosName, page.getResources().getColorSpace(cosName).getNumberOfComponents());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//                page.getResources().getExtGStateNames().forEach(cosName -> TauLogger.console("ExtGState: %s", cosName));
//                page.getResources().getFontNames().forEach(cosName -> TauLogger.console("Font: %s", cosName));
//                page.getResources().getShadingNames().forEach(cosName -> TauLogger.console("Shading: %s", cosName));
//                page.getResources().getPatternNames().forEach(name -> {
//                    System.out.println(name);
//                    try {
//                        System.out.println(page.getResources().getPattern(name).getCOSObject().keySet());
//                        System.out.println(page.getResources().getPattern(name).getCOSObject().getValues());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
                page.getContentStreams().forEachRemaining(pdStream -> {
                    try {
                        InputStream inputStream = pdStream.createInputStream();
                        OutputStream outputStream = pdStream.createOutputStream();
                        Charset charset = Charset.forName("windows-1251");
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset.newDecoder());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, charset.newEncoder());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        List<String> lineList = bufferedReader.lines().toList();
                        lineList.forEach(line -> {
                            try {
                                int index = lineIndex.getAndAdd(1);
                                if (index < templateLineList.size()) {
                                    String templateLine = templateLineList.get(index);
                                    outputStreamWriter.write(templateLine.contains("#findByPreviousLine")
                                            ? findByPreviousLine(templateLineList.get(index - 1), templateLine, lineList)
                                            : templateLine);
                                }
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
                });
                try {
                    page.setContents(pdStreamList);
                    document.save(editedFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String findByPreviousLine(String previousLine, String currentLine, List<String> lineList) {
        String defaultValue = currentLine.replace("#findByPreviousLine or ", "");
        int foundIndex = IntStream.range(0, lineList.size()).boxed()
                .filter(index -> lineList.get(index).equals(previousLine)).findFirst().orElse(-1);
        return foundIndex < 0 ? defaultValue : lineList.get(foundIndex + 1);
    }

    private static void writeToFile(int count, Map.Entry<COSName, COSBase> entry) {
        String name = entry.getKey().getName();
        String space = " ".repeat(count);
        COSBase cosBase = entry.getValue();
        if (cosBase instanceof COSDictionary cosDictionary) {
            System.out.printf("%s%s%n", space, name);
            cosDictionary.entrySet().forEach(cosNameCOSBaseEntry -> writeToFile(count + 1, cosNameCOSBaseEntry));
        } else if (cosBase instanceof COSObject cosObject) {
            COSBase baseObject = cosObject.getCOSObject();
            if (baseObject instanceof COSDictionary cosDictionary) {
                cosDictionary.entrySet().forEach(cosNameCOSBaseEntry -> writeToFile(count + 1, cosNameCOSBaseEntry));
            } else {
                System.out.printf("%s%s: %s%n", space, name, baseObject);
            }
        } else {
            COSBase baseObject = cosBase.getCOSObject();
            if (baseObject instanceof COSDictionary cosDictionary) {
                cosDictionary.entrySet().forEach(cosNameCOSBaseEntry -> writeToFile(count + 1, cosNameCOSBaseEntry));
            } else {
                System.out.printf("%s%s: %s%n", space, name, baseObject);
            }
        }

    }
}
