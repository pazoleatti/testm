package com.aplana.sbrf.taxaccounting.util;

import java.text.*;
import java.util.Locale;
import java.util.TimeZone;

public class FormatUtils {
	private static ThreadLocal<DateFormat> shortDateFormat = new ThreadLocal<DateFormat>();
	private static ThreadLocal<NumberFormat> simpleNumberFormat = new ThreadLocal<NumberFormat>();

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
			format = new DecimalFormat("0.########", new DecimalFormatSymbols(Locale.US));
			simpleNumberFormat.set(format);
		}
		return format;
	}
}
