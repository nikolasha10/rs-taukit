package rs.taukit.utils;

public class TimeStringParser {

    public static double getTime(String timeString) {
        String[] chunks = timeString.split(" ");
        double value = Double.parseDouble(chunks[0]);
        int multiplier = 1000;
        if (chunks[1].contains("м")) multiplier *= 60;
        else if (chunks[1].contains("ч")) multiplier *= 3600;
        return value * multiplier;
    }
}
