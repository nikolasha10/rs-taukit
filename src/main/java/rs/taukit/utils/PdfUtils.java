package rs.taukit.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class PdfUtils {
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
                page.getContentStreams().forEachRemaining(pdStream -> {
                    try {
                        InputStream inputStream = pdStream.createInputStream();
                        OutputStream outputStream = pdStream.createOutputStream();
                        Charset charset = Charset.forName("windows-1251");
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset.newDecoder());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, charset.newEncoder());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        bufferedReader.lines().forEach(line -> {
                            try {
                                int index = lineIndex.getAndAdd(1);
                                if (index < templateLineList.size()) {
                                    String templateLine = templateLineList.get(index);
                                    outputStreamWriter.write(templateLine.contains("#currentValue") ? line : templateLine);
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
}
