package rs.taukit.utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.cucumber.datatable.DataTable;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.rendering.PDFRenderer;
import rs.taukit.config.Config;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.page.playwright.PlaywrightPageObject;
import rs.taukit.records.ImageDifferences;
import rs.taukit.testdata.TestDataFile;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
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

    public static void comparePdfFiles(String testedFilePath, String standardFilePath) {
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

                    File htmlTemplate = new File("${baseResourcesDir}/templates/htmlTemplate.html");
                    String attachment = eval(Files.readString(htmlTemplate.toPath()));
                    TauLogger.warn(() -> TauLogger.attachFile("Различия", attachment.getBytes()), "страница %d отличается от ожидаемой", i);
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

    public static List<ImageDifferences> comparePdf(String testedFilePath,
                                                    String standardFilePath,
                                                    String ignoredZonesParameterName,
                                                    String autoReplacingParameterName,
                                                    boolean validateVisual,
                                                    boolean validateText) {
        Map<String, List<Map<String, Integer>>> ignoredZones = new HashMap<>();
        if (ignoredZonesParameterName != null) {
            TestDataFile ignoredZonesFileData = scenarioData().get(ignoredZonesParameterName);
            ignoredZones = YamlParser.parse(ignoredZonesFileData.getFilePath());
        }
        List<ImageDifferences> images = new ArrayList<>();


        try {
            PDDocument testedDocument = PDDocument.load(new File(testedFilePath));
            PDFRenderer testedRenderer = new PDFRenderer(testedDocument);
            PDDocument standardDocument = PDDocument.load(new File(standardFilePath));
            PDFRenderer standardRenderer = new PDFRenderer(standardDocument);
            int testedPagesNumber = testedDocument.getNumberOfPages();
            int standardPagesNumber = standardDocument.getNumberOfPages();
            if (testedPagesNumber != standardPagesNumber) {
                String errorMessage = String.format(
                        "Количество страниц тестируемого файла (%d) не равно количеству страниц эталонного (%d). ",
                        testedPagesNumber, standardPagesNumber);
                scenarioData().put("Проверка PDF", "Ошибки при сравнении PDF", errorMessage);
            }
            for (int pageNumber = 1; pageNumber <= Math.max(testedPagesNumber, standardPagesNumber); pageNumber++) {
                if (validateVisual) {
                    addVisualDifference(
                            pageNumber,
                            testedPagesNumber, standardPagesNumber,
                            testedRenderer, standardRenderer, ignoredZones, images);
                }
                if (validateText) {
                    addTextDifference(
                            pageNumber,
                            testedPagesNumber, standardPagesNumber,
                            testedFilePath, standardFilePath, autoReplacingParameterName, images);
                }
            }
            testedDocument.close();
            standardDocument.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return images;
    }
    private static void addVisualDifference(int pageNumber,
                                            int testedPagesNumber,
                                            int standardPagesNumber,
                                            PDFRenderer testedRenderer,
                                            PDFRenderer standardRenderer,
                                            Map<String, List<Map<String, Integer>>> ignoredZones,
                                            List<ImageDifferences> images) throws IOException {
        BufferedImage testedImage;
        BufferedImage standardImage = null;
        if (pageNumber > testedPagesNumber) {
            standardImage = getPdfPageImage(standardRenderer, pageNumber - 1);
            testedImage = ImageUtils.generateCleanImage(standardImage.getWidth(), standardImage.getHeight());
        } else {
            testedImage = getPdfPageImage(testedRenderer, pageNumber - 1);
        }
        if (pageNumber > standardPagesNumber) {
            standardImage = ImageUtils.generateCleanImage(testedImage.getWidth(), testedImage.getHeight());
        } else if (standardImage == null) {
            standardImage = getPdfPageImage(standardRenderer, pageNumber - 1);
        }
        BufferedImage differencesImage;
        BufferedImage zonedTestedImage = ImageUtils.deepCopy(testedImage);
        BufferedImage zonedStandardImage = ImageUtils.deepCopy(standardImage);
        if (ignoredZones.containsKey("Страница " + pageNumber)) {
            List<Map<String, Integer>> ignoredZoneList = ignoredZones.get("Страница " + pageNumber);
            List<String> ignoredZonesDataList = new ArrayList<>();
            for (Map<String, Integer> zone : ignoredZoneList) {
                int x = zone.get("x");
                int y = zone.get("y");
                int width = zone.get("width");
                int height = zone.get("height");
                ImageUtils.drawZone(zonedTestedImage, x, y, width, height);
                ImageUtils.drawZone(zonedStandardImage, x, y, width, height);
                ignoredZonesDataList.add(String.format("{x: %d, y: %d, w: %d, h: %d}", x, y, width, height));
            }
            differencesImage = ImageUtils.getDifferencesImage(zonedTestedImage, zonedStandardImage);
            scenarioData().put("ignoredZonesData_" + pageNumber, String.join(", ", ignoredZonesDataList));
        } else {
            scenarioData().put("ignoredZonesData_" + pageNumber, "");
            differencesImage = ImageUtils.getDifferencesImage(testedImage, standardImage);
        }
        if (differencesImage != null) {
            String name = String.format("Страница %d. Визуальные различия.", pageNumber);
            images.add(new ImageDifferences(pageNumber, name, testedImage, standardImage, differencesImage));
        }
    }

    private static void addTextDifference(int pageNumber,
                                          int testedPagesNumber,
                                          int standardPagesNumber,
                                          String testedFilePath,
                                          String standardFilePath,
                                          String autoReplacingParameterName,
                                          List<ImageDifferences> images) throws IOException {
        // TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();

        String pdfTestedPageTextContent;
        if (pageNumber <= testedPagesNumber) {
            PdfReader testedReader = new PdfReader(testedFilePath);
            TextExtractionStrategy testedStrategy = new LocationTextExtractionStrategy();
            pdfTestedPageTextContent = PdfTextExtractor.getTextFromPage(testedReader, pageNumber, testedStrategy);
            testedReader.close();
        } else {
            pdfTestedPageTextContent = "";
        }
        String originalPdfStandardPageTextContent;
        if (pageNumber <= standardPagesNumber) {
            PdfReader standardReader = new PdfReader(standardFilePath);
            TextExtractionStrategy standardStrategy = new LocationTextExtractionStrategy();
            originalPdfStandardPageTextContent = PdfTextExtractor.getTextFromPage(standardReader, pageNumber, standardStrategy);
            standardReader.close();
        } else {
            originalPdfStandardPageTextContent = "";
        }
        String pdfStandardPageTextContent = TextUtils.replaceValues(originalPdfStandardPageTextContent, autoReplacingParameterName);
        if (!pdfTestedPageTextContent.equals(pdfStandardPageTextContent)){
            BufferedImage pdfTestedPageTextImage = ImageUtils.textToImage(pdfTestedPageTextContent, pdfStandardPageTextContent);
            BufferedImage pdfStandardPageTextImage = ImageUtils.textToImage(pdfStandardPageTextContent, pdfTestedPageTextContent);
            BufferedImage differencesImage = ImageUtils.getDifferencesImage(pdfTestedPageTextImage, pdfStandardPageTextImage);
            String name = String.format("Страница %d. Текстовые различия.", pageNumber);
            images.add(new ImageDifferences(pageNumber, name, pdfTestedPageTextImage, pdfStandardPageTextImage, differencesImage));
        }
        if (Config.getBooleanProperty("pdf.text.content.files.create")) {
            String pdfTestedPageTextContentFilePath = String.format("target/temp/%s/%s/tested/page_%d.txt",
                    scenario().getFunctionality().replaceAll("[\\[\\]]", ""),
                    scenario().getName(),
                    pageNumber);
            FileUtils.writeFile(pdfTestedPageTextContentFilePath, pdfTestedPageTextContent);

            String pdfStandardPageTextContentFilePath = String.format("target/temp/%s/%s/standard/page_%d.txt",
                    scenario().getFunctionality().replaceAll("[\\[\\]]", ""),
                    scenario().getName(),
                    pageNumber);
            FileUtils.writeFile(pdfStandardPageTextContentFilePath, pdfStandardPageTextContent);
        }
    }

    private static BufferedImage getPdfPageImage(PDFRenderer renderer, int pageNumber) throws IOException {
        return renderer.renderImageWithDPI(pageNumber, Config.getIntegerProperty("pdf.convert.dpi"));
    }

    public static List<String> getPageContentFilePaths(String fileName, String filePath) {
        List<String> pageContentFilePathList = new ArrayList<>();
        try {
            PdfReader reader = new PdfReader(filePath);
            // не забываем, что нумерация страниц в PDF начинается с единицы.
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
//                TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
                TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
                String pdfPageTextContent = PdfTextExtractor.getTextFromPage(reader, i, strategy);

                String pdfPageTextContentFilePath = String.format("target/temp/%s/%s/%s/page_%d.txt",
                        scenario().getFunctionality().replaceAll("[\\[\\]]", ""),
                        scenario().getName(),
                        fileName.replaceAll("-", "_").replaceAll("\\.pdf", ""),
                        i);
                pageContentFilePathList.add(pdfPageTextContentFilePath);
                FileUtils.createFile(pdfPageTextContentFilePath);
                FileUtils.writeFile(pdfPageTextContentFilePath, pdfPageTextContent);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pageContentFilePathList;
    }
}
