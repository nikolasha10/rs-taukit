package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.records.ImageDifferences;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.*;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class AllureBasicPrintFormStep extends BasicUtils {

    @Step("проверить документ '{testedFileParameterName}' на соответствие эталону")
    public void validate(String testedFileParameterName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры проверки", dataTable);
        String nameSpace = "Проверка PDF";
        scenarioData().put(dataTable, nameSpace);
        TestDataFile testedFileData = scenarioData().get(testedFileParameterName);
        String standardFileParameterName = scenarioData().get(nameSpace, "эталон");
        TestDataFile standardFileData = scenarioData().get(standardFileParameterName);
        TauLogger.attachPdfFile(testedFileData.getFileName(), testedFileData.getFilePath());
        TauLogger.attachPdfFile(standardFileData.getFileName(), standardFileData.getFilePath());
        String checkFullContent = scenarioData().getOptional(nameSpace, "сравнить графически");
        String checkTextContent = scenarioData().getOptional(nameSpace, "сравнить текст");
        boolean validateVisual = checkFullContent == null || !checkFullContent.equalsIgnoreCase("нет");
        boolean validateText = checkTextContent == null || !checkTextContent.equalsIgnoreCase("нет");
        String ignoredZonesParameterName = scenarioData().getOptional(nameSpace, "игнорируемые зоны");
        String autoReplacingParameterName = scenarioData().getOptional(nameSpace, "автозамена текста");
        Runnable validationResult = validatePdfFullContent(
                testedFileData,
                standardFileData,
                ignoredZonesParameterName,
                autoReplacingParameterName,
                validateVisual,
                validateText);
        if (validationResult != null) {
            String errorMessage = scenarioData().getOptional("Проверка PDF", "Ошибки при сравнении PDF");
            String message = Optional.ofNullable(errorMessage).orElse("");
            Set<String> differentPages = scenarioData().get(nameSpace, "Номера различающихся страниц");
            TauLogger.fail(validationResult, "%sСтраницы %s файла '%s' не соответствуют эталону",
                    message, differentPages, testedFileData.getFileName());
        } else {
            TauLogger.report("Содержимое файла '%s' соответствует эталону", testedFileData.getFileName());
        }
        TauLogger.resetNotCritical();
    }

    //    @Step("проверить содержимое файла '{this.testedFileName}' на соответствие эталону")
    public Runnable validatePdfFullContent(TestDataFile testedFileData,
                                           TestDataFile standardFileData,
                                           String ignoredZonesParameterName,
                                           String autoReplacingParameterName,
                                           boolean validateVisual,
                                           boolean validateText) {
        List<ImageDifferences> differences = PdfUtils.comparePdf(
                testedFileData.getFilePath(),
                standardFileData.getFilePath(),
                ignoredZonesParameterName,
                autoReplacingParameterName,
                validateVisual,
                validateText);
        if (!differences.isEmpty()) {
            Set<Integer> differentPages = differences.stream().map(ImageDifferences::pageNumber).collect(Collectors.toSet());
            scenarioData().put("Проверка PDF", "Номера различающихся страниц", differentPages);
            return () -> {
                for (ImageDifferences pageDifferences : differences) {
                    String pageNumber = String.valueOf(pageDifferences.pageNumber());
                    scenarioData().put("pageNumber", pageNumber);
                    scenarioData().put("ignoredZonesData", scenarioData().get("ignoredZonesData_" + pageNumber));
                    String attachment = ImageUtils.getCompareResult(
                            pageDifferences.tested(),
                            pageDifferences.expected(),
                            pageDifferences.differences());
                    TauLogger.attachFile(pageDifferences.name(), attachment.getBytes());
                }
            };
        }
        return null;
    }

    //    @Step("проверить текстовое содержимое файла '{this.testedFileName}' на соответствие эталону")
    public Runnable validatePdfTextContent(TestDataFile testedFileData, TestDataFile standardFileData, String instructionPath) {
        List<String> errorList = new ArrayList<>();
        List<String> testedFilePageList = PdfUtils.getPageContentFilePaths(
                testedFileData.getFileName(), testedFileData.getFilePath());
        List<String> standardFilePageList = PdfUtils.getPageContentFilePaths(
                standardFileData.getFileName(), standardFileData.getFilePath());
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
            return null;
        } else {
            scenarioData().put("Проверка PDF", "Ошибки при сравнении текстового содержимого PDF", errorList);
            return () -> {
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
        }

    }

    private String getFilePath(String fileName) {
        return String.format("%s/%s", FileUtils.getDownloadFilePath(), fileName);
    }

}
