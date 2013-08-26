package com.aplana.sbrf.taxaccounting.dao.impl.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
}
