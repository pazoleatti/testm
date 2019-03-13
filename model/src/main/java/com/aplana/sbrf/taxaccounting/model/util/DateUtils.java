package com.aplana.sbrf.taxaccounting.model.util;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;


public class DateUtils {

    // Принятые в системе обозначения для пустых дат.
    public static final String DATE_ZERO_AS_DATE = "01.01.1901";
    public static final String DATE_ZERO_AS_STRING = "00.00.0000";

    private static final String COMMON_DATE_FORMAT = "dd.MM.yyyy";
    private static final DateTimeFormatter COMMON_DATE_FORMATTER = DateTimeFormat.forPattern(COMMON_DATE_FORMAT);

    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter SQL_DATE_FORMATTER = DateTimeFormat.forPattern(SQL_DATE_FORMAT);

    private static final String COMMON_DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final DateTimeFormatter COMMON_DATE_TIME_FORMATTER = DateTimeFormat.forPattern(COMMON_DATE_TIME_FORMAT);


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
        return formatByDateFormatter(date, COMMON_DATE_FORMATTER);
    }

    /**
     * Строковое значение даты в формате "01.01.2000 23:59:59"
     * Для null возвращает пустую строку
     */
    public static String commonDateTimeFormat(Date date) {
        if (date == null) return "";
        LocalDateTime jodaDateTime = LocalDateTime.fromDateFields(date);
        return COMMON_DATE_TIME_FORMATTER.print(jodaDateTime);
    }

    /**
     * Если аргумент - дата, которой мы обозначаем нулевую, возвращает принятое для неё стрковое значение.
     */
    public static String formatPossibleZeroDate(Date date) {
        String commonFormat = commonDateFormat(date);
        if (commonFormat.equals(DATE_ZERO_AS_DATE)) {
            return DATE_ZERO_AS_STRING;
        } else {
            return commonFormat;
        }
    }

    /**
     * Если аргумент - дата, которой мы обозначаем нулевую, возвращает принятое для неё стрковое значение.
     * Реализация со специальным значением для null-аргумента
     */
    public static String formatPossibleZeroDate(Date date, String onNull) {
        if (date == null) {
            return onNull;
        }
        return formatPossibleZeroDate(date);
    }

    /**
     * @return строковое представление даты для Oracle и HSQLDB: date '2000-01-01'
     */
    public static String formatForSql(Date date) {
        String formattedDate = formatByDateFormatter(date, SQL_DATE_FORMATTER);
        return "date " + StringUtils.wrapIntoSingleQuotes(formattedDate);
    }

    /**
     * @return дата, отформатированная выбранным форматтером.
     */
    private static String formatByDateFormatter(Date date, DateTimeFormatter formatter) {
        LocalDate jodaDate = LocalDate.fromDateFields(date);
        return formatter.print(jodaDate);
    }
}
