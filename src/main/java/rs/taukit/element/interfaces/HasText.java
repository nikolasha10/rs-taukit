package rs.taukit.element.interfaces;

public interface HasText {
    String getText();

    boolean hasText(String expectedValue);

    Boolean containsText(String expectedText);

    Boolean matches(String format);
}
