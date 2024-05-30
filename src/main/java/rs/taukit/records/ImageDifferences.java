package rs.taukit.records;

import java.awt.image.BufferedImage;

public record ImageDifferences(int pageNumber,
                               String name,
                               BufferedImage tested,
                               BufferedImage expected,
                               BufferedImage differences) {
}
