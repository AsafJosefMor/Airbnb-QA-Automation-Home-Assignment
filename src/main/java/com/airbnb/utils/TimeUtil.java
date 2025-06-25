package com.airbnb.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeUtil {

    static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");

    /**
     * Convert a date "M/dd/yyyy" into "yyy-MM-dd".
     *
     * @param usDate the input string in M/d/yyyy format
     * @return the formatted string in yyyy-MM-dd
     * @throws DateTimeParseException if the input isn’t valid
     */
    public static String convertUsToIso(String usDate) {
        LocalDate ld = LocalDate.parse(usDate, fmt);
        return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Returns the date one week after the given US-format date (M/d/yyyy).
     * @param usDate the date in M/d/yyyy format
     * @return the new date one week later in the same format
     */
    public static String addWeeksToDate(String usDate, int weeks) {
        LocalDate d = LocalDate.parse(usDate, fmt);
        return d.plusWeeks(weeks).format(fmt);
    }
}
