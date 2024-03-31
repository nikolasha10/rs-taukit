package rs.taukit.validator;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import rs.taukit.config.Config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.driver;
import static rs.taukit.utils.BasicUtils.scenario;

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
//                    System.out.printf("%s: %s%n",font.getName(), pdResources.getFont(font));
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

    public void comparePdfDocuments() {
        driver().openBrowser(true);
        String testedPdfUrl = new File(testedFilePath).toURI().toString();
    }

}
