package com.xanite.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {


    private static final String ETC_UTC = "Etc/UTC";
    private static final String YYYY_M_MDD = "yyyyMMdd";

    /**
     * To get the current date in <b>yyyyMMdd</b> format
     *
     * @return Current date in <b>yyyyMMdd</b> format
     */
    public static String getCurrentDate() {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(YYYY_M_MDD);
        LocalDateTime dateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of(ETC_UTC));
        return zonedDateTime.format(dateTimeFormat);
    }

    /**
     * @param dateString in the format of <b>yyyyMMdd</b>
     * @return Difference between current date and the date passed as a parameter
     */
    public static long calculateNoOfDays(String dateString) {
        long difference = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_M_MDD);

        LocalDate currentDate = LocalDate.now(ZoneId.of(ETC_UTC));
        LocalDate anyDate = LocalDate.parse(dateString, formatter);

        difference = ChronoUnit.DAYS.between(anyDate, currentDate);

        return difference;
    }

    public static int noOfDaysInCurrentYear() {
        LocalDate currentDate = LocalDate.now(ZoneId.of(ETC_UTC));
        int year = currentDate.getYear();
        return (year % 4 == 0) ? 366 : 365;
    }
}
