package com.aplana.sbrf.taxaccounting.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class FormatUtils {
	private static ThreadLocal<DateFormat> isoDateFormat = new ThreadLocal<DateFormat>();
	
	public static DateFormat getIsoDateFormat() {
		DateFormat result = isoDateFormat.get(); 
		if (result == null) {
			TimeZone tz = TimeZone.getTimeZone("UTC");
			result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			result.setTimeZone(tz);
			isoDateFormat.set(result);
		}
		return result;
	}
}
