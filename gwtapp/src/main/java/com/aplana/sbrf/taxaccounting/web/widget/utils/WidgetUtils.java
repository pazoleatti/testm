package com.aplana.sbrf.taxaccounting.web.widget.utils;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * Общие методы-утилиты использующиеся многими виджетами
 * Специально не сокращал код условий что бы можно было легко проследить логику
 * @author aivanov
 */
public class WidgetUtils {

    public static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd.MM.yyyy");

    /**
     * Проверка невхождения даты в ограничивающий период
     *
     * @param startDate наччало огр периода
     * @param endDate   коннец огр периода
     * @param current   дату которую проверяют
     * @return тру если не входит, иначе фолс
     */
    public static Boolean isInLimitPeriod(Date startDate, Date endDate, Date current) {
        if (current != null) {
            if (startDate == null && endDate == null) {
                return true;
            }
            if (startDate != null && endDate != null) {
                return compareDates(current, startDate) > -1 &&  compareDates(current, endDate) < 1;
            } else {
                return (startDate != null && compareDates(current, startDate) > -1) ||
                        (endDate != null && compareDates(current, endDate) < 1);
            }
        } else {
            return false;
        }
    }
    /**
     * Сравнение объектов
     * @param before до
     * @param after после
     * @return тру если даты разные, false - если одинаковые
     */
    public static boolean isWasChange(Object before, Object after) {
        if (before == null && after == null) {
            return false;
        }
        if (before != null && after != null) {
            return before instanceof Date && after instanceof Date ? ((Date) before).compareTo(((Date) after)) != 0 : !before.equals(after);
        }
        return true;
    }

    /**
     * Сравнение дат без учета времени
     * @param before до
     * @param after после
     * @return тру если даты разные, false - если одинаковые
     */
    public static boolean isDateWasChange(Date before, Date after){
        if (before == null && after == null) {
            return false;
        }
        if (before != null && after != null) {
            return compareDates(before, after) != 0;
        }
        return true;
    }

    /**
     * Сравнение дат исключая значения времени
     * нет проверки на null
     * @param thisDate дата с временем
     * @param anotherDate дата с временем
     * @return 0 если равны, 1 если вторая больше первой, -1 если первая меньше второй
     */
    public static int compareDates(Date thisDate, Date anotherDate) {
        Date thisDatewithoutTime = getDateWithOutTime(thisDate);
        Date anotherDatewithoutTime = getDateWithOutTime(anotherDate);
        return thisDatewithoutTime.compareTo(anotherDatewithoutTime);
    }

    /**
     * Убирает у даты значение времени выставляя в 0:0:0
     * @param date дата с временем
     * @return дата без времени
     */
    public static Date getDateWithOutTime(Date date) {
        return date != null ? dateTimeFormat.parse(dateTimeFormat.format(date)) : null;
    }

    /**
     * Возвращает строку с датой в виде dd.MM.yyyy
     * @param date дата
     * @return строка вида 23.12.2013
     */
    public static String getDateString(Date date) {
        return date != null ? dateTimeFormat.format(date) : null;
    }



}
