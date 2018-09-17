package com.aplana.sbrf.taxaccounting.model.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat sqlDateFormatter = new SimpleDateFormat(SQL_DATE_FORMAT);

    /**
     * @return строковое представление даты в одинарных кавычках: '01.01.2000'
     */
    public static String formatForSql(Date date) {
        String formattedDate = sqlDateFormatter.format(date);
        return "date " + StringUtils.wrapIntoSingleQuotes(formattedDate);
    }
}
