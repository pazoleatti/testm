package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class ParamUtils {
	
	private ParamUtils() {
	}

	public static Long getLong(PlaceRequest request, String key) {
		Long value = null;
		try {
			value = Long.valueOf(request.getParameter(key, ""));
		} catch (NumberFormatException e) {
			// Skip
		}
		return value;
	}

	public static Long getLong(PlaceRequest request, String key,
			Long defaultValue) {
		Long value = getLong(request, key);
		return value != null ? value : defaultValue;
	}

	public static Integer getInteger(PlaceRequest request, String key) {
		Integer value = null;
		try {
			value = Integer.valueOf(request.getParameter(key, ""));
		} catch (NumberFormatException e) {
			// Skip
		}
		return value;
	}

	public static Integer getInteger(PlaceRequest request, String key,
			Integer defaultValue) {
		Integer value = getInteger(request, key);
		return value != null ? value : defaultValue;
	}

}
