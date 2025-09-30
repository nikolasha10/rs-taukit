package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.commons.codec.digest.DigestUtils;
import rs.taukit.config.Config;
import rs.taukit.records.ImageDifferences;
import rs.taukit.testdata.TestData;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.ImageUtils;
import rs.taukit.utils.PdfUtils;
import rs.taukit.utils.TauLogger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class PdfManager extends StepManager {

    @Step("проверить документ '{testedFileParameterName}' на соответствие эталону")
    public void validate(String testedFileParameterName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры проверки", dataTable);
        String nameSpace = "Проверка PDF";
        scenarioData.put(dataTable, nameSpace);
        LinkedHashMap<String, String> info = new LinkedHashMap<>();
        TestData description = scenarioData.get("Описание");
        info.put("Функционал", description.get("Функционал"));
        info.put("Сценарий", description.get("Сценарий"));
        info.put("Шаг", description.get("Шаг"));
        String hash = DigestUtils.md5Hex(info.toString());
        String basePath = Config.getProperty("downloads.dir") + File.separator + hash;
        String filePath = basePath + "-file.pdf";
        LinkedHashMap<String, String> fileData = new LinkedHashMap<>();
        fileData.put("тип", "файл");
        fileData.put("путь", filePath);
        TestDataFile testedFileData = new TestDataFile(testedFileParameterName, fileData);
        String standardFileParameterName = scenarioData.get(nameSpace, "эталон");
        TestDataFile standardFileData = scenarioData.get(standardFileParameterName);
        TauLogger.attachPdfFile(testedFileParameterName + " (тест)", testedFileData.getFilePath());
        TauLogger.attachPdfFile(testedFileParameterName + " (эталон)", standardFileData.getFilePath());
        String ignoredZonesParameterName = scenarioData.getOptional(nameSpace, "игнорируемые зоны");
        Runnable validationResult = validatePdfFullContent(
                testedFileData,
                standardFileData,
                ignoredZonesParameterName);
        if (validationResult != null) {
            String errorMessage = scenarioData.getOptional("Проверка PDF", "Ошибки при сравнении PDF");
            String message = Optional.ofNullable(errorMessage).orElse("");
            List<String> differentPages = scenarioData.get(nameSpace, "Номера различающихся страниц");
            TauLogger.fail(validationResult, "%sСтраницы %s файла '%s' не соответствуют эталону",
                    message, differentPages, testedFileParameterName);
        } else {
            TauLogger.console("Содержимое файла '%s' соответствует эталону", testedFileParameterName);
        }
        TauLogger.resetNotCritical();
    }

    @Step("сравнить количество страниц в PDF-файле '{testedFileParameterName}' с ожидаемым значением '{expectedPageCount}'")
    public void validatePdfPageCount(String testedFileParameterName, String expectedPageCount) {
        TestDataFile testedFileData = scenarioData.get(testedFileParameterName);
        String testedFilePath = testedFileData.getFilePath();
        try {
            int expectedPageCountInt = Integer.parseInt(expectedPageCount);
            int actualPageCount = FileUtils.getPdfPageCount(testedFilePath);
            if (actualPageCount != expectedPageCountInt) {
                TauLogger.fail(
                        "Количество страниц в файле '%s' не совпадает. Ожидалось: %d, фактически: %d",
                        testedFileParameterName, expectedPageCountInt, actualPageCount
                );
            } else {
                TauLogger.console(
                        "Количество страниц в файле '%s' соответствует ожидаемому значению: %s",
                        testedFileParameterName, expectedPageCount
                );
            }
        } catch (Exception e) {
            TauLogger.fail(e, "Произошла ошибка при обработке PDF-файла '%s'", testedFileParameterName);
        }
    }

    //    @Step("проверить содержимое файла '{this.testedFileName}' на соответствие эталону")
    public Runnable validatePdfFullContent(TestDataFile testedFileData,
                                           TestDataFile standardFileData,
                                           String ignoredZonesParameterName) {
        List<ImageDifferences> differences = PdfUtils.comparePdf(
                testedFileData.getFilePath(),
                standardFileData.getFilePath(),
                ignoredZonesParameterName);
        if (!differences.isEmpty()) {
            List<Integer> differentPages = differences.stream().map(ImageDifferences::pageNumber).sorted().collect(Collectors.toList());
            scenarioData.put("Проверка PDF", "Номера различающихся страниц", differentPages);
            scenarioData.put("testName", scenario.getFunctionality() + " " + scenario.getName());
            return () -> {
                for (ImageDifferences pageDifferences : differences) {
                    String pageNumber = String.valueOf(pageDifferences.pageNumber());
                    scenarioData.put("pageNumber", pageNumber);
                    scenarioData.put("ignoredZonesData", scenarioData.get("ignoredZonesData_" + pageNumber));
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
            scenarioData.put("Проверка PDF", "Ошибки при сравнении текстового содержимого PDF", errorList);
            return () -> {
                for (int i = 0; i < differentPages.size(); i++) {
                    Map<String, BufferedImage> imageMap = images.get(i);
                    String pageNumber = String.valueOf(differentPages.get(i));
                    scenarioData.put("pageNumber", pageNumber);
                    scenarioData.put("ignoredZonesData", "");
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

    @Step("конвертировать pdf-файл 'fileParameterName' в текстовый формат")
    protected void convertPdfToTxt(String fileParameterName) {
        String filePath = PdfUtils.convertPdfToTxt(fileParameterName);
        LinkedHashMap<String, String> fileData = new LinkedHashMap<>();
        fileData.put("тип", "файл");
        fileData.put("путь", filePath);
        String fileName = fileParameterName.replaceAll("\\.pdf", ".txt");
        TestDataFile testDataFile = new TestDataFile(fileName, fileData);
        scenarioData().put(fileName, testDataFile);
        TauLogger.attachFile(fileName, filePath);
        TauLogger.console("Файл '%s' конвертирован в '%s'", fileParameterName, fileName);
    }
}
