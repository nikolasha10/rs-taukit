package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.scenario.ScenarioManager;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static rs.taukit.utils.BasicUtils.element;
import static rs.taukit.utils.BasicUtils.eval;

public class ServiceManager extends StepManager {
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

    public void createReport() {
        scenarioData.put("scenarioData", ScenarioManager.getInstance().generateFinalReport());
        scenarioData.put("jenkinsPipelineUrl", Config.getProperty("jenkins.pipeline.url"));
        scenarioData.put("jenkinsAuth", Config.getProperty("jenkins.auth"));
        List<Map<String, ?>> standList = YamlParser.parse(Config.eval("${baseResourcesDir}/configs/stands.yaml"));
        String stands = standList.stream().map((item -> "'" + item.get("Название").toString() + "'")).toList().toString();
        scenarioData.put("stands", stands);
        scenarioData.put("currentStand", Config.getSTAND().getName());
        scenarioData.put("branches", Config.getProperty("branches"));
        scenarioData.put("defaultBranch", Config.getProperty("defaultBranch"));
        String template = FileUtils.readFile("src/main/resources/templates/finalReportTemplate.html");
        FileUtils.writeFile("target/temp/finalReport.html", eval(template));
        TauLogger.attachFile("Отчёт", "target/temp/finalReport.html", "html");
        createXLSXReport("target/temp/report.xlsx");
    }

    private void createXLSXReport(String xlsxFilePath) {
        try {
            Map<String, Object> jsonData = JsonParser.parse(scenarioData.get("scenarioData"));

            // Создание новой книги Excel
            Workbook workbook = new XSSFWorkbook();
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
            Cell statisticCell = headerRow.createCell(0);
            statisticCell.setCellStyle(statisticStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            headerRow = sheet.createRow(rowNum++);
            sheet.createFreezePane(0, 2);
            Cell cell = headerRow.createCell(0);
            cell.setCellValue("Статус");
            cell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            cell = headerRow.createCell(4);
            cell.setCellValue("Сценарий");
            cell.setCellStyle(headerStyle);

            cell = headerRow.createCell(5);
            cell.setCellValue("Причина");
            cell.setCellStyle(headerStyle);

            cell = headerRow.createCell(6);
            cell.setCellValue("Категория ошибки");
            cell.setCellStyle(headerStyle);

            CellStyle suiteNameStyle = workbook.createCellStyle();
            suiteNameStyle.setFont(boldFont);
            suiteNameStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusPassedStyle = workbook.createCellStyle();
            statusPassedStyle.setFont(greenFont);
            statusPassedStyle.setAlignment(HorizontalAlignment.CENTER);
            statusPassedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusFailedStyle = workbook.createCellStyle();
            statusFailedStyle.setFont(redFont);
            statusFailedStyle.setAlignment(HorizontalAlignment.CENTER);
            statusFailedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle statusSkippedStyle = workbook.createCellStyle();
            statusSkippedStyle.setAlignment(HorizontalAlignment.CENTER);
            statusSkippedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            statusSkippedStyle.setFont(greyFont);

            CellStyle statusUnknownStyle = workbook.createCellStyle();
            statusUnknownStyle.setAlignment(HorizontalAlignment.CENTER);
            statusUnknownStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle hyperlinkStyle = workbook.createCellStyle();
            hyperlinkStyle.setFont(hyperlinkFont);
            hyperlinkStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle yellowBackgroundStyle = workbook.createCellStyle();
            yellowBackgroundStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            yellowBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
            blueBackgroundStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            blueBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            blueBackgroundStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setFont(blackFont);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            List<Map.Entry<String, Object>> parentSuiteEntryList = jsonData.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).toList();
            int testCount = 0;
            int passedCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            int analysisCount = 0;
            for (Map.Entry<String, Object> parentSuiteEntry : parentSuiteEntryList) {
                Row parentSuiteRow = sheet.createRow(rowNum);
                parentSuiteRow.createCell(0);
                Cell parentSuiteCell = parentSuiteRow.createCell(1);
                parentSuiteCell.setCellValue(parentSuiteEntry.getKey());
                parentSuiteCell.setCellStyle(suiteNameStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 4));
                rowNum++;
                Map<String, Object> parentSuiteContainer = YamlParser.convert(parentSuiteEntry.getValue());
                Map<String, Object> parentSuite = YamlParser.convert(parentSuiteContainer.get("testSuiteResults"));
                List<Map.Entry<String, Object>> suiteEntryList = parentSuite.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()).toList();
                for (Map.Entry<String, Object> suiteEntry : suiteEntryList) {
                    if (!suiteEntry.getKey().equals("-")) {
                        Row suiteRow = sheet.createRow(rowNum);
                        suiteRow.createCell(0);
                        suiteRow.createCell(1);
                        Cell suiteCell = suiteRow.createCell(2);
                        suiteCell.setCellValue(suiteEntry.getKey());
                        suiteCell.setCellStyle(suiteNameStyle);
                        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2, 4));
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
                            subSuiteRow.createCell(1);
                            subSuiteRow.createCell(2);
                            Cell subSuiteCell = subSuiteRow.createCell(3);
                            subSuiteCell.setCellValue(subSuiteEntry.getKey());
                            subSuiteCell.setCellStyle(suiteNameStyle);
                            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 3, 4));
                            rowNum++;
                        }
                        Map<String, Object> subSuiteContainer = YamlParser.convert(subSuiteEntry.getValue());
                        Map<String, Object> subSuite = YamlParser.convert(subSuiteContainer.get("testResults"));
                        List<Map.Entry<String, Object>> testEntryList = subSuite.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey()).toList();
                        for (Map.Entry<String, Object> testEntry : testEntryList) {
                            Row testRow = sheet.createRow(rowNum);
                            Cell statusCell = testRow.createCell(0);
                            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
                            Cell testCell = testRow.createCell(4);
                            testCell.setCellValue(testEntry.getKey().replaceAll("&quot;", "\""));
                            testCell.setCellStyle(cellStyle);
                            testCount++;
                            Map<String, Object> test = YamlParser.convert(testEntry.getValue());
                            String status = test.get("status") != null ? test.get("status").toString().toLowerCase() : "skipped";
                            switch (status) {
                                case "passed":
                                    passedCount++;
                                    statusCell.setCellStyle(statusPassedStyle);
                                    statusCell.setCellValue(status);
                                    break;
                                case "failed", "unknown", "broken":
                                    failedCount++;
                                    statusCell.setCellStyle(statusFailedStyle);
                                    statusCell.setCellValue("failed");
                                    break;
                                case "skipped":
                                    skippedCount++;
                                    statusCell.setCellStyle(statusSkippedStyle);
                                    statusCell.setCellValue(status);
                                    break;
                                default:
                                    statusCell.setCellStyle(cellStyle);
                            }
                            if (test.get("errorMessage") != null) {
                                Cell reasonCell = testRow.createCell(5);
                                reasonCell.setCellValue(test.get("errorMessage").toString());
                                reasonCell.setCellStyle(cellStyle);
                                String errorCategory = test.get("errorCategory") != null
                                        ? test.get("errorCategory").toString() : "Ожидается анализ";
                                Cell categoryCell = testRow.createCell(6);
                                categoryCell.setCellStyle(cellStyle);
                                if (errorCategory.contains("http")) {
                                    Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
                                    hyperlink.setAddress(errorCategory);
                                    String issueNumber = Arrays.stream(errorCategory.split("/")).toList().getLast();
                                    categoryCell.setHyperlink(hyperlink);
                                    categoryCell.setCellValue("Инцидент " + issueNumber);
                                    categoryCell.setCellStyle(hyperlinkStyle);
                                } else {
                                    categoryCell.setCellValue(errorCategory);
                                    if (errorCategory.equals("Ожидается анализ")) {
                                        analysisCount++;
                                        categoryCell.setCellStyle(yellowBackgroundStyle);
                                    } else if (errorCategory.equals("Ожидается подготовка ТУ")) {
                                        categoryCell.setCellStyle(blueBackgroundStyle);
                                    }
                                }
                            }
                            rowNum++;
                        }
                    }
                }
            }
            String statistic = String.format("""
                            Всего тестов: %d.   Пройдено: %d.   Провалено: %d.   Пропущено: %d.   Процент выполнения: %.0f%%
                            .   Требуется анализ: %d""",
                    testCount, passedCount, failedCount, skippedCount, 100.0 * passedCount / testCount, analysisCount);
            statisticCell.setCellValue(statistic);
            statisticCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
            CellRangeAddress statusFilter = new CellRangeAddress(1, rowNum - 1, 0, 0);
            sheet.setAutoFilter(statusFilter);

            sheet.setColumnWidth(0, 750);
            sheet.setColumnWidth(1, 750);
            sheet.setColumnWidth(2, 750);
            sheet.setColumnWidth(3, 750);
            sheet.setColumnWidth(4, 20000);
            sheet.setColumnWidth(5, 30000);
            sheet.setColumnWidth(6, 10000);

            // Сохранение файла
            File file = new File(eval(xlsxFilePath));
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();
            TauLogger.attachFile("XLSX Отчёт", xlsxFilePath, "xlsx");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании XLSX-отчёта из JSON: " + e.getMessage(), e);
        }
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
