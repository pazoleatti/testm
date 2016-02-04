package com.aplana.sbrf.taxaccounting.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Утилита для работы с датами
 * @author dloshkarev
 */
public final class SimpleDateUtils {

    private SimpleDateUtils() {}

    public static Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    public static int daysBetween(Date d1, Date d2){
        return (int)( (toStartOfDay(d2).getTime() - toStartOfDay(d1).getTime()) / (1000 * 60 * 60 * 24));
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

    /**
     * Сбросить время на начало суток
     *
     * @param date дата со временем
     * @return дату со временем, установленым на начало суток
     */
    public static Date toStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
