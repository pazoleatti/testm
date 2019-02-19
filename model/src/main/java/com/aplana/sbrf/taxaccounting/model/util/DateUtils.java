package com.aplana.sbrf.taxaccounting.model.util;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;


public class DateUtils {

    private static final String COMMON_DATE_FORMAT = "dd.MM.yyyy";
    private static final DateTimeFormatter COMMON_DATE_FORMATTER = DateTimeFormat.forPattern(COMMON_DATE_FORMAT);

    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter SQL_DATE_FORMATTER = DateTimeFormat.forPattern(SQL_DATE_FORMAT);


    /**
     * Строковое значение даты в формате "01.01.2000"
     * См. {@link #commonDateFormat(Date, String)}
     * Для null возвращает пустую строку.
     */
    public static String commonDateFormat(Date date) {
        return commonDateFormat(date, "");
    }

    /**
     * Строковое значение даты в формате "01.01.2000"
     *
     * @param date   дата
     * @param onNull значение, которое нужно вернуть, если передан null
     */
    public static String commonDateFormat(Date date, String onNull) {
        if (date == null) {
            return onNull;
        }
        return formatByFormatter(date, COMMON_DATE_FORMATTER);
    }

    /**
     * @return строковое представление даты для Oracle и HSQLDB: date '2000-01-01'
     */
    public static String formatForSql(Date date) {
        String formattedDate = formatByFormatter(date, SQL_DATE_FORMATTER);
        return "date " + StringUtils.wrapIntoSingleQuotes(formattedDate);
    }

    /**
     * @return дата, отформатированная выбранным форматтером.
     */
    private static String formatByFormatter(Date date, DateTimeFormatter formatter) {
        LocalDate jodaDate = LocalDate.fromDateFields(date);
        return formatter.print(jodaDate);
    }
}
