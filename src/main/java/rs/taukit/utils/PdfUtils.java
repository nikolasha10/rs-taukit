package rs.taukit.utils;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                TauLogger.console("Страница: %d", pageIndex);
                COSDictionary cosDictionary = page.getResources().getCOSObject();
                cosDictionary.entrySet().forEach(entry -> writeToFile(0, entry));
//                try {
//                    System.out.println(page.getResources().getFont(COSName.getPDFName("F3")).getCOSObject().keySet());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                page.getResources().getXObjectNames().forEach(cosName -> TauLogger.console("XObject: %s", cosName));
//                page.getResources().getPropertiesNames().forEach(cosName -> TauLogger.console("Propertie: %s", cosName));
//                page.getResources().getPatternNames().forEach(cosName -> TauLogger.console("Pattern: %s", cosName));
//                page.getResources().getColorSpaceNames().forEach(cosName -> {
//                    try {
//                        TauLogger
//                                .console("ColorSpace: %s - %d", cosName, page.getResources().getColorSpace(cosName).getNumberOfComponents());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//                page.getResources().getExtGStateNames().forEach(cosName -> TauLogger.console("ExtGState: %s", cosName));
//                page.getResources().getFontNames().forEach(cosName -> TauLogger.console("Font: %s", cosName));
//                page.getResources().getShadingNames().forEach(cosName -> TauLogger.console("Shading: %s", cosName));
//                page.getResources().getPatternNames().forEach(name -> {
//                    System.out.println(name);
//                    try {
//                        System.out.println(page.getResources().getPattern(name).getCOSObject().keySet());
//                        System.out.println(page.getResources().getPattern(name).getCOSObject().getValues());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
                page.getContentStreams().forEachRemaining(pdStream -> {
                    try {
                        InputStream inputStream = pdStream.createInputStream();
                        OutputStream outputStream = pdStream.createOutputStream();
                        Charset charset = Charset.forName("windows-1251");
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset.newDecoder());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, charset.newEncoder());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        List<String> lineList = bufferedReader.lines().toList();
                        lineList.forEach(line -> {
                            try {
                                int index = lineIndex.getAndAdd(1);
                                if (index < templateLineList.size()) {
                                    String templateLine = templateLineList.get(index);
                                    outputStreamWriter.write(templateLine.contains("#findByPreviousLine")
                                            ? findByPreviousLine(templateLineList.get(index - 1), templateLine, lineList)
                                            : templateLine);
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

    private static String findByPreviousLine(String previousLine, String currentLine, List<String> lineList) {
        String defaultValue = currentLine.replace("#findByPreviousLine or ", "");
        int foundIndex = IntStream.range(0, lineList.size()).boxed()
                .filter(index -> lineList.get(index).equals(previousLine)).findFirst().orElse(-1);
        return foundIndex < 0 ? defaultValue : lineList.get(foundIndex + 1);
    }

    private static void writeToFile(int count, Map.Entry<COSName, COSBase> entry){
        String name = entry.getKey().getName();
        String space = " ".repeat(count);
        COSBase cosBase = entry.getValue();
        if (cosBase instanceof COSDictionary cosDictionary){
            System.out.printf("%s%s%n", space, name);
            cosDictionary.entrySet().forEach(cosNameCOSBaseEntry -> writeToFile(count+1, cosNameCOSBaseEntry));
        } else if (cosBase instanceof COSObject cosObject){
            COSBase baseObject = cosObject.getCOSObject();
            if (baseObject instanceof COSDictionary cosDictionary){
                cosDictionary.entrySet().forEach(cosNameCOSBaseEntry -> writeToFile(count+1, cosNameCOSBaseEntry));
            } else {
                System.out.printf("%s%s: %s%n", space, name, baseObject);
            }
        } else {
            COSBase baseObject = cosBase.getCOSObject();
            if (baseObject instanceof COSDictionary cosDictionary){
                cosDictionary.entrySet().forEach(cosNameCOSBaseEntry -> writeToFile(count+1, cosNameCOSBaseEntry));
            } else {
                System.out.printf("%s%s: %s%n", space, name, baseObject);
            }
        }

    }
}
