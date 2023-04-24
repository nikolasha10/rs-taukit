package rs.taukit.utils;

import rs.taukit.config.Config;

public class TimeStringParser {

    public static double getTime(String timeString){
        if (timeString.equalsIgnoreCase("по умолчанию")){
            timeString = Config.getProperty("pageLoadingTimeout");
        }
        String[] chunks = timeString.split(" ");
        double value = Double.parseDouble(chunks[0]);
        int multiplier = 1000;
        if (chunks[1].contains("мин")) multiplier *= 60;
        else if (chunks[1].contains("час")) multiplier *= 3600;
        return value * multiplier;
    }
}
