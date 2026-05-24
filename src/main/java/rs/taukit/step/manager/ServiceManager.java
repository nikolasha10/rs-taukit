package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.scenario.ScenarioManager;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.element;
import static rs.taukit.utils.BasicUtils.eval;

public class ServiceManager extends StepManager {
    private final Map<String, String> statistics = new HashMap<>();

    public void skipActionsIfTrue(DataTable dataTable) {
        run(() -> {
            Predicate<Map<String, String>> predicate = condition -> new TestCondition(condition).checkResult().isConditionMet();
            if (dataTable.asMaps().stream().allMatch(predicate)) {
                scenarioData.put("SKIP_ACTIONS", true);
            }
        });
    }

    public void stopSkipActions() {
        Runnable runnable = () -> scenarioData.remove("SKIP_ACTIONS");
        runnable.run();
        scenarioStorage.saveStepAction(runnable);
    }

    public void createReport(String tagName) {
        scenarioData.put("scenarioData", ScenarioManager.getInstance().generateFinalReport(tagName));
        statistics.put("testRunDuration", scenarioData.get("testRunDuration"));
        statistics.put("durationDelta", scenarioData.get("durationDelta"));
        scenarioData.put("jenkinsPipelineUrl", Config.getProperty("jenkins.pipeline.url"));
        scenarioData.put("jenkinsAuth", Config.getProperty("jenkins.auth"));
        List<Map<String, ?>> standList = YamlParser.parse(Config.eval("${baseResourcesDir}/configs/stands.yaml"));
        String stands = standList.stream().map((item -> "'" + item.get("Название").toString() + "'")).toList().toString();
        scenarioData.put("stands", stands);
        scenarioData.put("currentStand", Config.getSTAND().getName());
        scenarioData.put("branches", Config.getProperty("branches"));
        scenarioData.put("defaultBranch", Config.getProperty("defaultBranch"));
        String tagNameClean = tagName.isEmpty() ? tagName : tagName.substring(1);
        String template = FileUtils.readFile("src/main/resources/templates/finalReportTemplate.html");
        String htmlReportPath = String.format("target/temp/finalReport_%s.html", tagNameClean);
        FileUtils.writeFile(htmlReportPath, eval(template));
        TauLogger.attachFile( "Отчёт " + tagNameClean + ".html", htmlReportPath, "html");
        if (new File("target/temp/errors.yml").exists()) {
            TauLogger.attachYaml("Категории ошибок. " + tagNameClean, FileUtils.readFile("target/temp/errors.yml").getBytes());
        }
        String xlsxFilePath = String.format("target/temp/report_%s.xlsx", tagNameClean);
        createXLSXReport(xlsxFilePath);
        TauLogger.attachFile("Отчёт " + tagNameClean + ".xlsx", xlsxFilePath, "xlsx");
        FileUtils.writeFile(Path.of(String.format("target/temp/statistic_%s.json", tagNameClean)), getStatistics());
    }

    private byte[] getStatistics() {
        return JsonParser.write(statistics);
    }

    private void createXLSXReport(String xlsxFilePath) {
        try {
            Map<String, Object> jsonData = JsonParser.parse(scenarioData.get("scenarioData"));

            // Создание новой книги Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            POIXMLProperties xmlProps = workbook.getProperties();
            POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
            coreProps.setCreator("Tau-Kit Test Framework");

            Sheet sheet = workbook.createSheet("Результаты автотестов");

            // Стиль для заголовков
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            Font blackFont = workbook.createFont();
            blackFont.setColor(IndexedColors.BLACK.getIndex());

            Font whiteFont = workbook.createFont();
            whiteFont.setColor(IndexedColors.WHITE.getIndex());

            Font greenFont = workbook.createFont();
            greenFont.setColor(IndexedColors.GREEN.getIndex());

            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());

            Font greyFont = workbook.createFont();
            greyFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            Font yellowFont = workbook.createFont();
            yellowFont.setColor(IndexedColors.YELLOW.getIndex());

            Font hyperlinkFont = workbook.createFont();
            hyperlinkFont.setUnderline(Font.U_SINGLE);
            hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());

            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle statisticStyle = workbook.createCellStyle();
            statisticStyle.setFont(yellowFont);
            statisticStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            statisticStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            statisticStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 0;
            // Запись ключей как заголовков столбцов
            Row headerRow = sheet.createRow(rowNum++);
            sheet.createFreezePane(0, 1);
            Cell cell = headerRow.createCell(0);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("Автотест");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            cell = headerRow.createCell(4);
            cell.setCellStyle(headerStyle);
            cell = headerRow.createCell(5);
            cell.setCellValue("Статус");
            cell.setCellStyle(headerStyle);
            cell = headerRow.createCell(6);
            cell.setCellValue("Примечание");
            cell.setCellStyle(headerStyle);

            CellStyle suiteNameStyle = workbook.createCellStyle();
            suiteNameStyle.setFont(boldFont);
            suiteNameStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusPassedStyle = workbook.createCellStyle();
            statusPassedStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            statusPassedStyle.setFillPattern(FillPatternType.BIG_SPOTS);
            statusPassedStyle.setAlignment(HorizontalAlignment.CENTER);
            statusPassedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusFailedStyle = workbook.createCellStyle();
            statusFailedStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            statusFailedStyle.setFillPattern(FillPatternType.BIG_SPOTS);
            statusFailedStyle.setAlignment(HorizontalAlignment.CENTER);
            statusFailedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusSkippedStyle = workbook.createCellStyle();
            statusSkippedStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            statusSkippedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            statusSkippedStyle.setAlignment(HorizontalAlignment.CENTER);
            statusSkippedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusUnknownStyle = workbook.createCellStyle();
            statusUnknownStyle.setAlignment(HorizontalAlignment.CENTER);
            statusUnknownStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle hyperlinkStyle = workbook.createCellStyle();
            hyperlinkStyle.setFont(hyperlinkFont);
            hyperlinkStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle resultCellStyle = workbook.createCellStyle();
            resultCellStyle.setAlignment(HorizontalAlignment.CENTER);
            resultCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle yellowBackgroundStyle = workbook.createCellStyle();
            yellowBackgroundStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            yellowBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            yellowBackgroundStyle.setAlignment(HorizontalAlignment.CENTER);
            yellowBackgroundStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            yellowBackgroundStyle.setBorderBottom(BorderStyle.THIN);
            yellowBackgroundStyle.setBorderTop(BorderStyle.THIN);
            yellowBackgroundStyle.setBorderLeft(BorderStyle.THIN);
            yellowBackgroundStyle.setBorderRight(BorderStyle.THIN);
            yellowBackgroundStyle.setBottomBorderColor(IndexedColors.ORANGE.getIndex());
            yellowBackgroundStyle.setTopBorderColor(IndexedColors.ORANGE.getIndex());
            yellowBackgroundStyle.setLeftBorderColor(IndexedColors.ORANGE.getIndex());
            yellowBackgroundStyle.setRightBorderColor(IndexedColors.ORANGE.getIndex());

            CellStyle blueBackgroundStyle = workbook.createCellStyle();
            blueBackgroundStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            blueBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            blueBackgroundStyle.setAlignment(HorizontalAlignment.CENTER);
            blueBackgroundStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            blueBackgroundStyle.setBorderBottom(BorderStyle.THIN);
            blueBackgroundStyle.setBorderTop(BorderStyle.THIN);
            blueBackgroundStyle.setBorderLeft(BorderStyle.THIN);
            blueBackgroundStyle.setBorderRight(BorderStyle.THIN);
            blueBackgroundStyle.setBottomBorderColor(IndexedColors.BLUE.getIndex());
            blueBackgroundStyle.setTopBorderColor(IndexedColors.BLUE.getIndex());
            blueBackgroundStyle.setLeftBorderColor(IndexedColors.BLUE.getIndex());
            blueBackgroundStyle.setRightBorderColor(IndexedColors.BLUE.getIndex());

            CellStyle redBackgroundStyle = workbook.createCellStyle();
            redBackgroundStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            redBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            redBackgroundStyle.setAlignment(HorizontalAlignment.CENTER);
            redBackgroundStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            redBackgroundStyle.setBorderBottom(BorderStyle.THIN);
            redBackgroundStyle.setBorderTop(BorderStyle.THIN);
            redBackgroundStyle.setBorderLeft(BorderStyle.THIN);
            redBackgroundStyle.setBorderRight(BorderStyle.THIN);
            redBackgroundStyle.setBottomBorderColor(IndexedColors.RED.getIndex());
            redBackgroundStyle.setTopBorderColor(IndexedColors.RED.getIndex());
            redBackgroundStyle.setLeftBorderColor(IndexedColors.RED.getIndex());
            redBackgroundStyle.setRightBorderColor(IndexedColors.RED.getIndex());

            CellStyle tanBackgroundStyle = workbook.createCellStyle();
            tanBackgroundStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
            tanBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tanBackgroundStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            tanBackgroundStyle.setWrapText(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setFont(blackFont);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            String[] statusValues = {
                    "Ожидается анализ",
                    "В работе",
                    "Ожидается подготовка ТУ",
                    "Ожидается исправление АТ",
                    "Ожидается актуализация ТК",
                    "Ожидается консультация РТ",
                    "Сбой",
                    "Зарегистрирован дефект"};

            List<Map.Entry<String, Object>> parentSuiteEntryList = jsonData.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).toList();
            int testCount = 0;
            int newTestCount = 0;
            int passedCount = 0;
            int newPassedCount = 0;
            int failedCount = 0;
            int newFailedCount = 0;
            int skippedCount = 0;
            int newSkippedCount = 0;
            int analysisCount = 0;
            int preparationCount = 0;
            int defectCount = 0;
            int previousPassedCount = 0;
            int previousFailedCount = 0;
            int previousSkippedCount = 0;
            Map<String, String> statusIcons = Map.of(
                    "passed", "✔",
                    "failed", "✖",
                    "unknown", "✖",
                    "broken", "✖",
                        "skipped", "≫");
            Map<String, Font> statusStyles = Map.of(
                    "passed", greenFont,
                    "failed", redFont,
                    "unknown", redFont,
                    "broken", redFont,
                    "skipped", blackFont);
            for (Map.Entry<String, Object> parentSuiteEntry : parentSuiteEntryList) {
                Row parentSuiteRow = sheet.createRow(rowNum);
                Cell parentSuiteCell = parentSuiteRow.createCell(0);
                parentSuiteCell.setCellValue(parentSuiteEntry.getKey());
                parentSuiteCell.setCellStyle(suiteNameStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
                rowNum++;
                Map<String, Object> parentSuiteContainer = YamlParser.convert(parentSuiteEntry.getValue());
                Map<String, Object> parentSuite = YamlParser.convert(parentSuiteContainer.get("testSuiteResults"));
                List<Map.Entry<String, Object>> suiteEntryList = parentSuite.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()).toList();
                for (Map.Entry<String, Object> suiteEntry : suiteEntryList) {
                    if (!suiteEntry.getKey().equals("-")) {
                        Row suiteRow = sheet.createRow(rowNum);
                        suiteRow.createCell(0);
                        Cell suiteCell = suiteRow.createCell(1);
                        suiteCell.setCellValue(suiteEntry.getKey());
                        suiteCell.setCellStyle(suiteNameStyle);
                        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 3));
                        rowNum++;
                    }
                    Map<String, Object> suiteContainer = YamlParser.convert(suiteEntry.getValue());
                    Map<String, Object> suite = YamlParser.convert(suiteContainer.get("testSubSuiteResults"));
                    List<Map.Entry<String, Object>> subSuiteEntryList = suite.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey()).toList();
                    for (Map.Entry<String, Object> subSuiteEntry : subSuiteEntryList) {
                        if (!subSuiteEntry.getKey().equals("-")) {
                            Row subSuiteRow = sheet.createRow(rowNum);
                            subSuiteRow.createCell(0);
                            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
                            Cell subSuiteCell = subSuiteRow.createCell(2);
                            subSuiteCell.setCellValue(subSuiteEntry.getKey());
                            subSuiteCell.setCellStyle(suiteNameStyle);
                            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2, 3));
                            rowNum++;
                        }
                        Map<String, Object> subSuiteContainer = YamlParser.convert(subSuiteEntry.getValue());
                        Map<String, Object> subSuite = YamlParser.convert(subSuiteContainer.get("testResults"));
                        List<Map.Entry<String, Object>> testEntryList = subSuite.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey()).toList();
                        for (Map.Entry<String, Object> testEntry : testEntryList) {
                            Row testRow = sheet.createRow(rowNum);
                            testRow.createCell(0);
                            Cell testCell = testRow.createCell(3);
                            testCell.setCellValue(testEntry.getKey().replaceAll("&quot;", "\""));
                            testCell.setCellStyle(cellStyle);
                            testCount++;
                            Map<String, Object> test = YamlParser.convert(testEntry.getValue());
                            String result = test.get("status") != null ? test.get("status").toString().toLowerCase() : "skipped";
                            String lastResult = test.get("lastStatus") != null
                                    ? test.get("lastStatus").toString().toLowerCase()
                                    : "skipped";
                            if (test.get("lastStatus") == null) {
                                newTestCount++;
                            } else {
                                switch (lastResult) {
                                    case "passed": previousPassedCount++; break;
                                    case "failed", "unknown", "broken": previousFailedCount++; break;
                                    case "skipped": previousSkippedCount++; break;
                                }
                            }
                            switch (result) {
                                case "passed":
                                    passedCount++;
                                    if (!lastResult.equals(result)) {
                                        newPassedCount++;
                                    }
//                                    resultCell.setCellStyle(statusPassedStyle);
                                    break;
                                case "failed", "unknown", "broken":
                                    failedCount++;
                                    if (Arrays.asList("failed", "unknown", "broken").contains(lastResult)) {
                                        lastResult = result;
                                    }
                                    if (!lastResult.equals(result)) {
                                        newFailedCount++;
                                    }
//                                    resultCell.setCellStyle(statusFailedStyle);
                                    break;
                                case "skipped":
                                    skippedCount++;
                                    if (!lastResult.equals(result)) {
                                        newSkippedCount++;
                                    }
//                                    resultCell.setCellStyle(statusSkippedStyle);
                                    break;
                            }
                            Cell resultCell = testRow.createCell(4);
                            resultCell.setCellStyle(cellStyle);
                            RichTextString resultCellText;
                            if (result.equals(lastResult)) {
                                resultCellText = workbook.getCreationHelper().createRichTextString(statusIcons.get(result));
                                resultCellText.applyFont(0, 1, statusStyles.get(result));
                            } else {
                                String text = statusIcons.get(lastResult) + " ➝ " + statusIcons.get(result);
                                resultCellText = workbook.getCreationHelper().createRichTextString(text);
                                resultCellText.applyFont(0, 1, statusStyles.get(lastResult));
                                resultCellText.applyFont(2, 3, blackFont);
                                resultCellText.applyFont(4, 5, statusStyles.get(result));
                            }
//                            resultCellText.applyFont(whiteFont);
                            resultCell.setCellValue(resultCellText);
                            resultCell.setCellStyle(resultCellStyle);
                            if (test.get("errorMessage") != null) {
                                rowNum++;
                                Row errorRow = sheet.createRow(rowNum);
                                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum, 0, 2));
                                Cell reasonCell = errorRow.createCell(3);
                                Cell resultCell2 = errorRow.createCell(4);
                                resultCell2.setCellValue(resultCell.getStringCellValue());
                                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum, 4, 4));
                                Cell statusCell = testRow.createCell(5);
                                statusCell.setCellStyle(resultCellStyle);
                                Cell statusCell2 = errorRow.createCell(5);
                                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum, 5, 5));
                                Cell noteCell = testRow.createCell(6);
                                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum, 6, 6));
                                String errorMessage = test.get("errorMessage").toString()
                                        .replaceAll("&quot;", "\"")
                                        .replaceAll("<br>", "\n");
                                reasonCell.setCellValue(errorMessage);
                                reasonCell.setCellStyle(tanBackgroundStyle);
                                String errorCategory = test.get("errorCategory") != null
                                        ? test.get("errorCategory").toString() : "Ожидается анализ";
                                String note = test.get("note") != null ? test.get("note").toString() : "";
                                statusCell.setCellValue(errorCategory);
                                noteCell.setCellValue(note);
                                noteCell.setCellStyle(cellStyle);
                                Matcher matcher = Pattern.compile(".*(https://.*)").matcher(note.toLowerCase());
                                if (matcher.matches()) {
                                    Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
                                    hyperlink.setAddress(matcher.group(1));
                                    noteCell.setHyperlink(hyperlink);
                                }
                                statusCell2.setCellStyle(statusCell.getCellStyle());
                                switch (errorCategory) {
                                    case "Ожидается анализ": analysisCount++; break;
                                    case "Ожидается подготовка ТУ": preparationCount++; break;
                                    case "Зарегистрирован дефект": defectCount++; break;
                                }
                            } else {
                                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));
                            }
                            rowNum++;
                        }
                    }
                }
            }
            CellRangeAddress statusFilter = new CellRangeAddress(0, rowNum - 1, 4, 5);
            sheet.setAutoFilter(statusFilter);

            DataValidationConstraint dataValidationConstraint = dataValidationHelper.createExplicitListConstraint(statusValues);
            CellRangeAddressList range = new CellRangeAddressList(1, rowNum - 1, 5, 5);
            DataValidation dataValidation = dataValidationHelper.createValidation(dataValidationConstraint, range);
            dataValidation.setSuppressDropDownArrow(true);
            sheet.addValidationData(dataValidation);

            addConditionalFormatting(sheet, range, "F2=\"Ожидается анализ\"", IndexedColors.LIGHT_YELLOW, IndexedColors.ORANGE);
            addConditionalFormatting(sheet, range, "F2=\"Зарегистрирован дефект\"", IndexedColors.CORAL, IndexedColors.RED);
            addConditionalFormatting(sheet, range, "F2=\"Ожидается подготовка ТУ\"", IndexedColors.LIGHT_CORNFLOWER_BLUE, IndexedColors.BLUE);

            sheet.setColumnWidth(0, 600);
            sheet.setColumnWidth(1, 600);
            sheet.setColumnWidth(2, 600);
            sheet.setColumnWidth(3, 30000);
            sheet.setColumnWidth(4, 2600);
            sheet.setColumnWidth(5, 10000);
            sheet.setColumnWidth(6, 20000);

            CellStyle statisticHeaderStyle = workbook.createCellStyle();
            statisticHeaderStyle.setFont(boldFont);
            statisticHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            statisticHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statisticCellStyle = workbook.createCellStyle();
            statisticCellStyle.setAlignment(HorizontalAlignment.CENTER);

            Sheet statisticSheet = workbook.createSheet("Статистика");
            rowNum = 0;
            Row statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.setRowStyle(resultCellStyle);
            statisticRow.createCell(1).setCellValue("Всего");
            statisticRow.createCell(2).setCellValue("%");
            statisticRow.createCell(3).setCellValue("Новых");
            statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.createCell(0).setCellValue("Всего тестов");
            statisticRow.createCell(1).setCellValue(testCount);
            statistics.put("Всего тестов", String.valueOf(testCount));
            statistics.put("Новых тестов", String.valueOf(newTestCount));
            statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.createCell(0).setCellValue("Успешных");
            String passedCountString = String.format("%.0f %%", 100.0 * passedCount / testCount);
            statisticRow.createCell(1).setCellValue(passedCount);
            statisticRow.createCell(2).setCellValue(passedCountString);
            statisticRow.createCell(3).setCellValue(newPassedCount);
            statistics.put("Успешных", String.valueOf(passedCount));
            statistics.put("% успешных", passedCountString);
            statistics.put("Новых успешных", String.valueOf(newPassedCount));
            statistics.put("Прошлых успешных", String.valueOf(previousPassedCount));
            statistics.put("Прошлых проваленных", String.valueOf(previousFailedCount));
            statistics.put("Прошлых пропущенных", String.valueOf(previousSkippedCount));
            statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.createCell(0).setCellValue("Проваленных");
            String failedCountString = String.format("%.0f %%", 100.0 * failedCount / testCount);
            statisticRow.createCell(1).setCellValue(failedCount);
            statisticRow.createCell(2).setCellValue(failedCountString);
            statisticRow.createCell(3).setCellValue(newFailedCount);
            statistics.put("Проваленных", String.valueOf(failedCount));
            statistics.put("% проваленных", failedCountString);
            statistics.put("Новых проваленных", String.valueOf(newFailedCount));
            statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.createCell(0).setCellValue("Пропущенных");
            String skippedCountString = String.format("%.0f %%", 100.0 * skippedCount / testCount);
            statisticRow.createCell(1).setCellValue(skippedCount);
            statisticRow.createCell(2).setCellValue(skippedCountString);
            statisticRow.createCell(3).setCellValue(newSkippedCount);
            statistics.put("Пропущенных", String.valueOf(skippedCount));
            statistics.put("% пропущенных", skippedCountString);
            statistics.put("Новых пропущенных", String.valueOf(newSkippedCount));
            statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.createCell(0).setCellValue("Ожидается анализ");
            statisticRow.createCell(1).setCellFormula("COUNTIF('Результаты автотестов'!F2:F" + sheet.getLastRowNum() + ",\"Ожидается анализ\")");
            statistics.put("Ожидается анализ", String.valueOf(analysisCount));
            statisticRow = statisticSheet.createRow(rowNum++);
            statisticRow.createCell(0).setCellValue("Ожидается подготовка ТУ");
            statisticRow.createCell(1).setCellFormula("COUNTIF('Результаты автотестов'!F2:F" + sheet.getLastRowNum() + ",\"Ожидается подготовка ТУ\")");
            statistics.put("Ожидается подготовка ТУ", String.valueOf(preparationCount));
            statisticRow = statisticSheet.createRow(rowNum);
            statisticRow.createCell(0).setCellValue("Зарегистрирован дефект");
            statisticRow.createCell(1).setCellFormula("COUNTIF('Результаты автотестов'!F2:F" + sheet.getLastRowNum() + ",\"Зарегистрирован дефект\")");
            statistics.put("Зарегистрирован дефект", String.valueOf(defectCount));

            statisticSheet.setDefaultColumnStyle(0, suiteNameStyle);
            statisticSheet.setDefaultColumnStyle(1, statisticCellStyle);
            statisticSheet.setDefaultColumnStyle(2, statisticCellStyle);
            statisticSheet.setDefaultColumnStyle(3, statisticCellStyle);
            Row statisticHeaderRow = statisticSheet.getRow(0);
            for (int i = 1; i < 4; i++) {
                statisticHeaderRow.getCell(i).setCellStyle(statisticHeaderStyle);
            }

            statisticSheet.setColumnWidth(0, 8000);
            statisticSheet.setColumnWidth(1, 2000);
            statisticSheet.setColumnWidth(2, 2000);
            statisticSheet.setColumnWidth(3, 2000);

            // Сохранение файла
            File file = new File(eval(xlsxFilePath));
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании XLSX-отчёта из JSON: " + e.getMessage(), e);
        }
    }

    private void addConditionalFormatting(Sheet sheet, CellRangeAddressList range, String formula, IndexedColors backgroundColor, IndexedColors borderColor) {
        SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule = formatting.createConditionalFormattingRule(formula);
        setPatternFormatting(rule, backgroundColor);
        setBorderFormatting(rule, borderColor);
        formatting.addConditionalFormatting(range.getCellRangeAddresses(), rule);
    }

    private static void setPatternFormatting(ConditionalFormattingRule rule, IndexedColors backgroundColor) {
        PatternFormatting patternFormatting = rule.createPatternFormatting();
        patternFormatting.setFillBackgroundColor(backgroundColor.getIndex());
        patternFormatting.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
    }

    private static void setBorderFormatting(ConditionalFormattingRule rule, IndexedColors borderColor) {
        BorderFormatting borderFormatting = rule.createBorderFormatting();
        borderFormatting.setBorderBottom(BorderStyle.THIN);
        borderFormatting.setBorderTop(BorderStyle.THIN);
        borderFormatting.setBorderLeft(BorderStyle.THIN);
        borderFormatting.setBorderRight(BorderStyle.THIN);
        borderFormatting.setBottomBorderColor(borderColor.getIndex());
        borderFormatting.setTopBorderColor(borderColor.getIndex());
        borderFormatting.setLeftBorderColor(borderColor.getIndex());
        borderFormatting.setRightBorderColor(borderColor.getIndex());
    }

    protected void forceClickElement(String elementName) {
        run(() -> {
            IElement element = element(elementName);
            element.forceClick();
            TauLogger.report("[примечание] нажата ЛКМ над элементом '%s'", elementName);
        }, "Не удалось кликнуть в области над элементом '%s'", elementName);
    }

    protected void createTestItAutotest() {
        List<CucumberScenario> scenarioList = ScenarioManager.getRequestedScenarioList();
        scenarioList.forEach(TestITUtils::createAutotest);
    }

    protected void updateTestItAutotest() {
        List<CucumberScenario> scenarioList = ScenarioManager.getRequestedScenarioList();
        scenarioList.forEach(TestITUtils::updateAutotest);
    }

    protected void setMutex(String mutexName) {
        scenario.setMutex(mutexName);
    }

    protected void releaseMutex(String mutexName) {
        scenario.releaseMutex(mutexName);
    }

    protected void addErrorCategories(DataTable dataTable) {
        scenarioData.put("ERROR_CATEGORIES", dataTable.asList());
    }

    protected void createCondition(String conditionName, DataTable dataTable) {
        scenarioData.put(conditionName, dataTable);
    }

    @Step("служебный шаг: начать запись трассировки")
    protected void startTraceRecord() {
        getBrowser().startTraceRecord();
    }

    @Step("служебный шаг: сохранить запись трассировки, как '{fileName}'")
    protected void stopTraceRecord(String fileName) {
        getBrowser().stopTraceRecord(fileName);
        TauLogger.attachFile(fileName, FileUtils.getDownloadFilePath() + "/" + fileName + ".zip");
    }
}
