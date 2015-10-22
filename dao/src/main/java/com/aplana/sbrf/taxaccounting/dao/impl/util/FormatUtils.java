package com.aplana.sbrf.taxaccounting.dao.impl.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FormatUtils {

	private static ThreadLocal<DateFormat> shortDateFormat = new ThreadLocal<DateFormat>();
	private static ThreadLocal<NumberFormat> simpleNumberFormat = new ThreadLocal<NumberFormat>();

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private FormatUtils() {
	}

	/**
	 * Форматирует строку в строку, содержащую информацию только о дате, без времени и тайм-зоны
	 * Используется при генерации строковых представлений даты, сохраняемых в БД
	 */
	public static DateFormat getShortDateFormat() {
		DateFormat result = shortDateFormat.get(); 
		if (result == null) {
			result = new SimpleDateFormat("yyyy-MM-dd");
			shortDateFormat.set(result);
		}
		return result;
	}

	/**
	 * Создает и возвращает числовой формат для простого десятичного формата чисел. Целая часть форматируется без
	 * разделителей.
	 *
	 * @return формат
	 */
	public static NumberFormat getSimpleNumberFormat(){
		NumberFormat format = simpleNumberFormat.get();
		if(format==null){
			format = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.US));
			format.setMaximumFractionDigits(38);
			simpleNumberFormat.set(format);
		}
		return format;
	}

    /**
     * Возвращает полное имя периода для нф с нарастающим итогом
     * @return
     */
    public static String getAccName(String name, Date calendarStartDate) {
        Calendar sDate = Calendar.getInstance();
        sDate.setTime(calendarStartDate);
        int day = sDate.get(Calendar.DAY_OF_MONTH);
        int month = sDate.get(Calendar.MONTH) + 1;
        if (day == 1 && month == 4) {
            //2 квартал: 2 квартал (полугодие)
            return name + " (полугодие)";
        } else if (day == 1 && month == 7) {
            //3 квартал: 3 квартал (9 месяцев)
            return name + " (9 месяцев)";
        } else if (day == 1 && month == 10) {
            //4 квартал: 4 квартал (год)
            return name + " (год)";
        } else {
            return name;
        }
    }
}
