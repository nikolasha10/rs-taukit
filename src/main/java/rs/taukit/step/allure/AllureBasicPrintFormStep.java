package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
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
        scenarioData().put(dataTable);
        String standardBaseFilePath = scenarioData().get("эталон");
        TauLogger.attachPdfFile(testedFileName, testedFilePath);
        TauLogger.attachPdfFile(standardFileName, standardBaseFilePath);
        if (validatePdfFullContent(standardBaseFilePath)) {
            validatePdfTextContent(standardBaseFilePath);
        }
    }

    //    @Step("проверить содержимое файла '{this.testedFileName}' на соответствие эталону")
    public boolean validatePdfFullContent(String standardBaseFilePath) {
        List<ImageDifferences> differences = PdfUtils.comparePdf(testedFilePath, standardBaseFilePath);
        List<Integer> differentPages = new ArrayList<>();
        if (differences.isEmpty()) {
            TauLogger.report("Содержимое файла '%s' соответствует эталону");
            return true;
        } else {
            Runnable runnable = () -> {
                for (int i = 0; i < differences.size(); i++) {
                    ImageDifferences pageDifferences = differences.get(i);
                    String attachment = ImageUtils.getCompareResult(
                            pageDifferences.tested(),
                            pageDifferences.expected(),
                            pageDifferences.differences());
                    TauLogger.attachFile("Страница " + pageDifferences.pageNumber(), attachment.getBytes());
                    differentPages.add(pageDifferences.pageNumber());
                }
            };
            TauLogger.fail(runnable, "Страницы %s файла '%s' не соответствуют эталону", testedFileName);
            return false;
        }
    }

    //    @Step("проверить текстовое содержимое файла '{this.testedFileName}' на соответствие эталону")
    public List<String> validatePdfTextContent(String standardBaseFilePath) {
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
            String instructionPath = scenarioData().getOptional("инструкция-текст");
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
        String downloadDirPath = Config.getProperty("downloads.dir");
        String scenarioName = scenario().getName();
        return String.format("%s/%s/%s", downloadDirPath, scenarioName, fileName);
    }

}
