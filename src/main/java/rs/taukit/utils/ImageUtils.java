package rs.taukit.utils;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.scenarioData;

public class ImageUtils {
    public static BufferedImage textFileToImage(String filePath, String longerString, int maxLineNumber) {
        List<String> lines = FileUtils.readFileToLineList(filePath);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 14);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(longerString) + 20;
        int height = fm.getAscent() + fm.getHeight() * maxLineNumber + 20;
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setBackground(Color.WHITE);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < lines.size(); i++) {
            g2d.drawString(lines.get(i), 10, fm.getAscent() + fm.getHeight() * (i + 1));
        }
        g2d.dispose();
        return img;
//        try {
//            ImageIO.write(img, "png", new File(filePath.replace(".txt", ".png")));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }

    public static BufferedImage getDifferencesImage(BufferedImage testedImage, BufferedImage standardImage) {
        Screenshot testedScreenshot = new Screenshot(testedImage);
        Screenshot standardScreenshot = new Screenshot(standardImage);
        ImageDiff diff = new ImageDiffer().makeDiff(standardScreenshot, testedScreenshot);
        if (diff.hasDiff()) {
            return diff.getMarkedImage();
        } else return null;
    }

    public static String getCompareResult(BufferedImage testedImage, BufferedImage standardImage, BufferedImage differencesImage) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(differencesImage, "png", byteArrayOutputStream);
            byteArrayOutputStream.close();
            scenarioData().put("differences", "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            ByteArrayOutputStream testedBytes = new ByteArrayOutputStream();
            ImageIO.write(testedImage, "png", testedBytes);
            testedBytes.close();
            scenarioData().put("tested", "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(testedBytes.toByteArray()));
            ByteArrayOutputStream standardBytes = new ByteArrayOutputStream();
            ImageIO.write(standardImage, "png", standardBytes);
            standardBytes.close();
            scenarioData().put("expected", "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(standardBytes.toByteArray()));
            File htmlTemplate = new File("src/main/resources/templates/htmlTemplate.html");
            return eval(Files.readString(htmlTemplate.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Что-то пошло не так", e);
        }
    }
}
