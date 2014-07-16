package com.aplana.sbrf.taxaccounting.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Утилита для работы с датами
 * @author dloshkarev
 */
public class SimpleDateUtils {
    public static Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    public static int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static java.sql.Date getSqlDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    public static Date getMinDate(Date first, Date second) {
        if (first.before(second)) {
            return first;
        } else {
            return second;
        }
    }

    public static Date getMaxDate(Date first, Date second) {
        if (first.after(second)) {
            return first;
        } else {
            return second;
        }
    }
}
