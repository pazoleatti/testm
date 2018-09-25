package com.aplana.sbrf.taxaccounting.model.util;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtils {

    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter sqlDateFormatter = DateTimeFormat.forPattern(SQL_DATE_FORMAT);

    /**
     * @return строковое представление даты для Oracle и HSQLDB: date '2000-01-01'
     */
    public static String formatForSql(Date date) {
        LocalDate jodaDate = LocalDate.fromDateFields(date);
        String formattedDate = sqlDateFormatter.print(jodaDate);
        return "date " + StringUtils.wrapIntoSingleQuotes(formattedDate);
    }
}
