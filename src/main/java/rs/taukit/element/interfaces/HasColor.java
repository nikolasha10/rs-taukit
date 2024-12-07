package rs.taukit.element.interfaces;

public interface HasColor {

    String getColor();

    boolean hasColor(String expectedValue);

    String getBgColor();

    boolean hasBgColor(String color);
}
