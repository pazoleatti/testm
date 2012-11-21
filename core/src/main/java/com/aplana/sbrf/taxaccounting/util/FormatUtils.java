package com.aplana.sbrf.taxaccounting.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class FormatUtils {
	private static ThreadLocal<DateFormat> isoDateFormat = new ThreadLocal<DateFormat>();
	private static ThreadLocal<DateFormat> shortDateFormat = new ThreadLocal<DateFormat>();
	
	/**
	 * Форматирует строку в формат ISO
	 * Используется при генерации строк при передаче в браузер (рекомендуемый способ для передачи дат в dojo)
	 */
	public static DateFormat getIsoDateFormat() {
		DateFormat result = isoDateFormat.get(); 
		if (result == null) {
			TimeZone tz = TimeZone.getTimeZone("UTC");
			result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			result.setTimeZone(tz);
			isoDateFormat.set(result);
		}
		return result;
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
	 * Функция преобразует список (например, содержащий элементы 1,2,3,4) в строку вида "(1,2,3,4)", которая бдует
	 * использоваться в SQL запросах вида "...where param in (1,2,3,4)";
	 */
	public static String transformToSqlInStatement(List list){
		StringBuffer stringBuffer = new StringBuffer(list.toString());
		return "(" + (stringBuffer.substring(1, stringBuffer.length() - 1)) + ")";
	}
}
