package rs.taukit.validator;

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
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.*;

/**
 * Класс для работы с pdf-документами
 */
public class PdfValidator {
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

    public PdfValidator(String testedFileName) {
        this.testedFileName = testedFileName;
        this.testedFilePath = getFilePath(testedFileName);
        this.templateFileName = this.testedFileName.replace(".pdf", " template.txt");
        this.templateFilePath = getFilePath(templateFileName);
        this.standardFileName = this.testedFileName.replace(".pdf", " standard.pdf");
        this.standardFilePath = getFilePath(standardFileName);
    }

    private String getFilePath(String fileName) {
        String downloadDirPath = Config.getProperty("downloads.dir");
        String scenarioName = testContext().getScenarioName();
        return String.format("%s/%s/%s", downloadDirPath, scenarioName, fileName);
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

    private void editPage(PDPage page) {
        this.currentPdPage = page;
        page.getContentStreams().forEachRemaining(this::editPageContentWrapper);
        page.setContents(pdStreamList);
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
//                    System.out.printf("%s: %s%n",font.getName(), pdResources.getFont(font));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            double xEnd = xStart + width.get();
            return String.format("%.2f %s", xEnd, matcher.group(4));
        } else {
            throw new RuntimeException(String.format("Формат строки '%s' не соответствует требованиям", line));
        }
    }

    public void comparePdfDocuments() {
        driver().openBrowser(true);
        String testedPdfUrl = new File(testedFilePath).toURI().toString();
    }

    @Step("проверить документ '{this.testedFileName}' на соответствие эталону")
    public void validate(DataTable dataTable) {
        TauLogger.attachDataTable("Параметры проверки", dataTable);
        scenarioData().put(dataTable);
        waitForDownload();
        String instructionName = scenarioData().get("инструкция");
        createPdfTemplate(templateFileName, instructionName);
        String standardBaseFilePath = scenarioData().get("эталон");
        createPdfStandard(standardBaseFilePath, templateFileName);
    }

    @Step("дождаться окончания загрузки файла '{this.testedFileName}'")
    public void waitForDownload() {
        new ExpectedResult(List.of("файл", this.testedFileName, "загружен")).waitForResult();
    }

}
