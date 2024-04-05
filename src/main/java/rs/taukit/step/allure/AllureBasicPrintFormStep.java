package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.records.ImageDifferences;
import rs.taukit.utils.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllureBasicPrintFormStep extends BasicUtils {
    private final String testedFileName;
    private final String testedFilePath;
    private final String standardFileName;

    public AllureBasicPrintFormStep(String testedFileName) {
        this.testedFileName = testedFileName;
        this.testedFilePath = getFilePath(testedFileName);
        this.standardFileName = this.testedFileName.replace(".pdf", " (эталон).pdf");
    }

    @Step("проверить документ '{this.testedFileName}' на соответствие эталону")
    public void validate(DataTable dataTable) {
        TauLogger.attachDataTable("Параметры проверки", dataTable);
        String nameSpace = "Проверка PDF";
        scenarioData().put(dataTable, nameSpace);
        String standardBaseFilePath = FileUtils.getTestFilePath(scenarioData().get(nameSpace, "эталон"));
        TauLogger.attachPdfFile(testedFileName, testedFilePath);
        TauLogger.attachPdfFile(standardFileName, standardBaseFilePath);
        String ignoredZones = scenarioData().getOptional(nameSpace, "игнорируемые зоны");
        String ignoredZonesFilePath = FileUtils.getTestFilePath(ignoredZones);
        validatePdfFullContent(standardBaseFilePath, ignoredZonesFilePath);
        String autoReplacing = scenarioData().getOptional(nameSpace, "автозамена текста");
        String autoReplacingFilePath = FileUtils.getTestFilePath(autoReplacing);
        validatePdfTextContent(standardBaseFilePath, autoReplacingFilePath);
        TauLogger.resetNotCritical();
    }

    //    @Step("проверить содержимое файла '{this.testedFileName}' на соответствие эталону")
    public boolean validatePdfFullContent(String standardBaseFilePath, String instructionPath) {
        List<ImageDifferences> differences = PdfUtils.comparePdf(testedFilePath, standardBaseFilePath, instructionPath);
        List<Integer> differentPages = new ArrayList<>();
        if (differences.isEmpty()) {
            TauLogger.report("Содержимое файла '%s' соответствует эталону", testedFileName);
            return true;
        } else {
            for (ImageDifferences pageDifferences : differences) {
                differentPages.add(pageDifferences.pageNumber());
            }
            Runnable runnable = () -> {
                for (ImageDifferences pageDifferences : differences) {
                    String pageNumber = String.valueOf(pageDifferences.pageNumber());
                    scenarioData().put("pageNumber", pageNumber);
                    scenarioData().put("ignoredZonesData", scenarioData().get("ignoredZonesData_" + pageNumber));
                    String attachment = ImageUtils.getCompareResult(
                            pageDifferences.tested(),
                            pageDifferences.expected(),
                            pageDifferences.differences());
                    TauLogger.attachFile("Страница " + pageDifferences.pageNumber(), attachment.getBytes());
                }
            };
            TauLogger.fail(runnable, "Страницы %s файла '%s' не соответствуют эталону",
                    differentPages, testedFileName);
            return false;
        }
    }

    //    @Step("проверить текстовое содержимое файла '{this.testedFileName}' на соответствие эталону")
    public List<String> validatePdfTextContent(String standardBaseFilePath, String instructionPath) {
        List<String> errorList = new ArrayList<>();
        List<String> testedFilePageList = PdfUtils.getPageContentFilePaths(testedFileName, testedFilePath);
        List<String> standardFilePageList = PdfUtils.getPageContentFilePaths(standardFileName, standardBaseFilePath);

        int testedFilePageSize = testedFilePageList.size();
        int standardFilePageSize = standardFilePageList.size();
        if (testedFilePageSize != standardFilePageSize) {
            errorList.add(String.format("Количество страниц (%d) не равно ожидаемому (%d)",
                    testedFilePageSize, standardFilePageSize));
        }

        List<Integer> differentPages = new ArrayList<>();
        List<Map<String, BufferedImage>> images = new ArrayList<>();
        for (int i = 0; i < Math.min(testedFilePageSize, standardFilePageSize); i++) {
            String testedPageFilePath = testedFilePageList.get(i);
            String standardPageFilePath = standardFilePageList.get(i);
            if (instructionPath != null) {
                FileUtils.replaceValues(standardPageFilePath, instructionPath);
            }
            List<String> pageErrorsList = FileUtils.compareTextFiles(testedPageFilePath, standardPageFilePath);
            if (!pageErrorsList.isEmpty()) {
                differentPages.add(i + 1);
                List<String> testedLines = FileUtils.readFileToLineList(testedPageFilePath);
                List<String> standardLines = FileUtils.readFileToLineList(standardPageFilePath);
                int maxLineCount = Math.max(testedLines.size(), standardLines.size());
                testedLines.addAll(standardLines);
                int longerLineIndex = 0;
                int maxLength = 0;
                for (int j = 0; j < testedLines.size(); j++) {
                    if (testedLines.get(j).length() > maxLength) {
                        longerLineIndex = j;
                        maxLength = testedLines.get(j).length();
                    }
                }
                String longerString = testedLines.get(longerLineIndex);
                BufferedImage testedImage = ImageUtils.textFileToImage(testedPageFilePath, longerString, maxLineCount);
                BufferedImage standardImage = ImageUtils.textFileToImage(standardPageFilePath, longerString, maxLineCount);
                BufferedImage differencesImage = ImageUtils.getDifferencesImage(testedImage, standardImage);
                if (differencesImage != null) {
                    images.add(Map.of(
                            "tested", testedImage,
                            "expected", standardImage,
                            "differences", differencesImage
                    ));
                }
            }
        }
        if (!differentPages.isEmpty()) {
            errorList.add(String.format("Содержимое страниц %s отличается от ожидаемого", differentPages));
        }
        if (errorList.isEmpty()) {
            TauLogger.report("Текстовое содержимое файла соответствует эталону");
        } else {
            Runnable runnable = () -> {
                for (int i = 0; i < differentPages.size(); i++) {
                    Map<String, BufferedImage> imageMap = images.get(i);
                    String pageNumber = String.valueOf(differentPages.get(i));
                    scenarioData().put("pageNumber", pageNumber);
                    scenarioData().put("ignoredZonesData", "");
                    String attachment = ImageUtils.getCompareResult(
                            imageMap.get("tested"),
                            imageMap.get("expected"),
                            imageMap.get("differences"));
                    TauLogger.attachFile("Страница " + differentPages.get(i), attachment.getBytes());
                }
            };
            TauLogger.fail(runnable, "Текстовое содержимое файла '%s' не соответствует эталону. %s",
                    testedFileName, String.join(" ", errorList));
        }
        return errorList;
    }

    private String getFilePath(String fileName) {
        return String.format("%s/%s", FileUtils.getDownloadFilePath(), fileName);
    }

}
