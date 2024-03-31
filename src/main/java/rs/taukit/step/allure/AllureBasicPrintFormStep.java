package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import rs.taukit.config.Config;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.testdata.TestDataInstruction;
import rs.taukit.utils.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class AllureBasicPrintFormStep extends BasicUtils {
    private final String testedFileName;
    private final String testedFilePath;
    private final String templateFileName;
    private final String templateFilePath;
    private final String standardFileName;
    private final String standardFilePath;
    private final AtomicInteger lineIndex = new AtomicInteger(0);
    private final List<PDStream> pdStreamList = new ArrayList<>();
    private List<String> templateLineList;
    private OutputStreamWriter outputStreamWriter;
    private PDPage currentPdPage;

    public AllureBasicPrintFormStep(String testedFileName) {
        this.testedFileName = testedFileName;
        this.testedFilePath = getFilePath(testedFileName);
        this.templateFileName = this.testedFileName.replace(".pdf", " template.txt");
        this.templateFilePath = getFilePath(templateFileName);
        this.standardFileName = this.testedFileName.replace(".pdf", " standard.pdf");
        this.standardFilePath = getFilePath(standardFileName);
    }

    @Step("проверить документ '{this.testedFileName}' на соответствие эталону")
    public void validate(DataTable dataTable) {
        TauLogger.attachDataTable("Параметры проверки", dataTable);
        scenarioData().put(dataTable);
        String standardBaseFilePath = scenarioData().get("эталон");
        TauLogger.attachPdfFile(testedFileName, testedFilePath);
        TauLogger.attachPdfFile(standardFileName, standardBaseFilePath);
        List<String> errorList = new ArrayList<>(validateTextContent(standardBaseFilePath));

//        waitForDownload();
//        String instructionName = scenarioData().get("инструкция");
//        createPdfTemplate(templateFileName, instructionName);

//        createPdfStandard(standardBaseFilePath, templateFileName);
    }

    @Step("проверить текстовое содержимое файла '{this.testedFileName}' на соответствие эталону")
    public List<String> validateTextContent(String standardBaseFilePath) {
        List<String> errorList = new ArrayList<>();
        List<String> testedFilePageList = PdfUtils.getPageContentFilePaths(testedFilePath);
        List<String> standardFilePageList = PdfUtils.getPageContentFilePaths(standardBaseFilePath);

        int testedFilePageSize = testedFilePageList.size();
        int standardFilePageSize = standardFilePageList.size();
        if (testedFilePageSize != standardFilePageSize) {
            errorList.add(String.format("Количество страниц (%d) не равно ожидаемому (%d)",
                    testedFilePageSize, standardFilePageSize));
        }

        List<Integer> differentPages = new ArrayList<>();
        for (int i = 0; i < Math.min(testedFilePageSize, standardFilePageSize); i++) {
            List<String> pageErrorsList = FileUtils.compareTextFiles(
                    testedFilePageList.get(i), standardFilePageList.get(i));
            if (!pageErrorsList.isEmpty()) {
                differentPages.add(i + 1);
                List<List<String>> dataTableList = new ArrayList<>();
                pageErrorsList.forEach(errorMessage -> dataTableList.add(Collections.singletonList(errorMessage)));
                DataTable pageErrorsTable = DataTable.create(dataTableList, TextUtils.tableConverter);
                String pageErrorsTableName = String.format("Содержимое страницы %d отличается от ожидаемого", i + 1);
                TauLogger.attachDataTable(pageErrorsTableName, pageErrorsTable);
                TauLogger.attachFile(String.format("Текст страницы %d (тест)", i + 1), testedFilePageList.get(i));
                TauLogger.attachFile(String.format("Текст страницы %d (ожидаемый)", i + 1), standardFilePageList.get(i));
            }
        }
        if (!differentPages.isEmpty()) {
            errorList.add(String.format("Содержимое страниц %s отличается от ожидаемого", differentPages));
        }
        if (errorList.isEmpty()) {
            TauLogger.report("Текстовое содержимое файла соответствует эталону");
        } else {
            TauLogger.fail("Текстовое содержимое файла '%s' не соответствует эталону", testedFileName);
        }
        return errorList;
    }

    private void parsePdfFileToTxt() {

    }

    @Step("дождаться окончания загрузки файла '{this.testedFileName}'")
    public void waitForDownload() {
        new ExpectedResult(List.of("файл", this.testedFileName, "загружен")).waitForResult();
    }

    /**
     * Формирует текстовый файл шаблона pdf-документа из фрагментов, перечисленных в "инструкции"
     *
     * @param templateName - название формируемого файла шаблона
     * @param instruction  - название параметра в тестовых данных, содержащего "инструкцию" по сборке шаблона
     */
    @Step("сформировать шаблон '{templateName}' в соответствии с инструкцией '{instruction}'")
    public void createPdfTemplate(String templateName, String instruction) {
        TestDataInstruction testDataInstruction = scenarioData().get(instruction);
        createPdfTemplate(templateName, testDataInstruction.getDataTable());
    }

    /**
     * Формирует текстовый файл шаблона pdf-документа из фрагментов, перечисленных в таблице данных
     *
     * @param templateName - название формируемого файла шаблона
     * @param dataTable    - таблица данных, вида <br><table><tr><td>| Директория </td><td>| Фрагмент </td><td>|</td></tr>
     *                     <tr><td>| <i>Папка с фрагментами </i></td><td>| <i>Название файла </i></td><td>|</td></tr></table>
     */
    public void createPdfTemplate(String templateName, DataTable dataTable) {
        FileUtils.createFile(templateFilePath);
        dataTable.asMaps().forEach(item -> {
            String templatesDir = Config.getProperty("templates.dir");
            String templateType = TestDataUtils.get(item, "Директория");
            String fragmentName = TestDataUtils.get(item, "Фрагмент");
            String fragmentPath = String.format("%s/%s/%s", templatesDir, templateType, fragmentName);
            FileUtils.appendFile(templateFilePath, FileUtils.readFile(fragmentPath));
        });
    }

    @Step("сформировать эталон '{this.standardFileName}' по файлу '{standardBaseFilePath}' и шаблону '{templateName}'")
    public void createPdfStandard(String standardBaseFilePath, String templateName) {
        String templateContent = eval(FileUtils.readFile(templateFilePath));
        File standardFile = new File(standardBaseFilePath);
        templateLineList = templateContent.lines().toList();
        try (PDDocument document = PDDocument.load(standardFile)) {
            PDPageTree pageTree = document.getDocumentCatalog().getPages();
            pageTree.forEach(this::editPage);
            document.save(standardFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Step("сравнить файл \"(.+)\" с эталоном \"(.+)\"")
    public void validatePrintForm(DataTable dataTable) {
        String printFormName = "Анкета-заявление на предоставление кредита (односторонняя)";
        try {
            Files.createDirectories(new File(String.format("${baseResourcesDir}/templates/%s", printFormName)).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(String.format("target/downloads/01.02 Отладка сравнения печатных форм/%s.pdf", printFormName));
        File fileResult = new File(String.format("target/downloads/01.02 Отладка сравнения печатных форм/%s после преобразования.pdf", printFormName));
        File fileTemplate = new File(String.format("${baseResourcesDir}/templates/%s.txt", printFormName));
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
                    File fileTemplatePage = new File(String.format("${baseResourcesDir}/templates/%s/page_%d.txt", printFormName, pageIndex + 1));
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

    private String getFilePath(String fileName) {
        String downloadDirPath = Config.getProperty("downloads.dir");
        String scenarioName = scenario().getName();
        return String.format("%s/%s/%s", downloadDirPath, scenarioName, fileName);
    }

    private void editPage(PDPage page) {
        this.currentPdPage = page;
        page.getContentStreams().forEachRemaining(this::editPageContentWrapper);
        page.setContents(this.pdStreamList);
    }

    private void editPageContentWrapper(PDStream pdStream) {
        try {
            editPageContent(pdStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void editPageContent(PDStream pdStream) throws IOException {
        OutputStream outputStream = pdStream.createOutputStream();
        String pdfEncoding = Config.getProperty("pdf.encoding");
        Charset charset = Charset.forName(pdfEncoding);
        outputStreamWriter = new OutputStreamWriter(outputStream, charset.newEncoder());
        templateLineList.forEach(this::outputStreamWriterWrapper);
        outputStreamWriter.close();
        outputStream.close();
        pdStreamList.add(pdStream);
    }

    private void outputStreamWriterWrapper(String line) {
        try {
            outputStreamWriter.write(line.contains("#computeWidth") ? computeWidth(line) : line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String computeWidth(String line) throws IOException {
        String format = "^#computeWidth\\((F\\d+)\\)=(\\d+\\.\\d+)\\+(.+)\\s(\\d+\\.\\d+ l)$";
        Matcher matcher = Pattern.compile(format).matcher(line);
        if (matcher.matches()) {
            String fontName = matcher.group(1);
            double xStart = Double.parseDouble(matcher.group(2));
            String text = matcher.group(3);
            PDResources pdResources = currentPdPage.getResources();
            AtomicReference<PDFont> pdFont = new AtomicReference<>();
            AtomicReference<Double> width = new AtomicReference<>();
            pdResources.getFontNames().forEach(font -> {
                try {
                    if (font.getName().equals(fontName)) {
                        pdFont.set(pdResources.getFont(font));
                        width.set((double) pdResources.getFont(font).getStringWidth(text));
                        System.out.println(width.get());
                    }
                    System.out.printf("%s: %s%n", font.getName(), pdResources.getFont(font));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            double xEnd = xStart + Optional.ofNullable(width.get()).orElse(0.0);
            return String.format("%.2f %s", xEnd, matcher.group(4));
        } else {
            throw new RuntimeException(String.format("Формат строки \"%s\" не соответствует требованиям", line));
        }
    }

}
