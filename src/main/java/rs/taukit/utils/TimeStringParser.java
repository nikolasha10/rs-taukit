package rs.taukit.utils;

public class TimeStringParser {

    public static double getTime(String timeString) {
        if (timeString.isEmpty()) return 0;
        String[] chunks = timeString.split(" ");
        double value = Double.parseDouble(chunks[0]);
        int multiplier = 1000;
        if (chunks[1].contains("м")) multiplier *= 60;
        else if (chunks[1].contains("ч")) multiplier *= 3600;
        return value * multiplier;
    }

    public static String formatInterval(long time){
        long seconds = time / 1000;
        long minutes = 0;
        long hours = 0;
        long days = 0;
        if (seconds >= 60){
            minutes = seconds / 60;
            seconds = seconds - minutes * 60;
            if (minutes >= 60){
                hours = minutes / 60;
                minutes = minutes - hours * 60;
            }
            if (hours >= 24){
                days = hours / 24;
                hours = hours - days * 24;
            }
        }
        String dayString = days > 0 ? days + " д " : "";
        String hourString = hours > 0 ? hours + " ч " : "";
        String minutesString = minutes > 0 ? minutes + " м " : "";
        String secondsString = seconds > 0 ? seconds + " с" : "меньше секунды";
        return String.format("%s%s%s%s", dayString, hourString, minutesString, secondsString);
    }
}
