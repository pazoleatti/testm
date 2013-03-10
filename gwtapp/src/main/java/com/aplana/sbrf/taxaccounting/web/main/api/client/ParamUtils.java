package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.gwtplatform.mvp.client.proxy.PlaceRequest;

/**
 * Утилиты для парсинга и преобразования параметров PlaceRequest
 * 
 * @author sgoryachkin
 *
 */
public final class ParamUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private ParamUtils() {
	}

	/**
	 * Извлечение параметра типа Long из PlaceRequest
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static Long getLong(PlaceRequest request, String key) {
		Long value = null;
		try {
			value = Long.valueOf(request.getParameter(key, ""));
		} catch (NumberFormatException e) {
			// Skip
		}
		return value;
	}

	/**
	 * Извлечение параметра типа Long из PlaceRequest
	 * 
	 * @param request
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static Long getLong(PlaceRequest request, String key,
			Long defaultValue) {
		Long value = getLong(request, key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Извлечение параметра типа Integer из PlaceRequest
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static Integer getInteger(PlaceRequest request, String key) {
		Integer value = null;
		try {
			value = Integer.valueOf(request.getParameter(key, ""));
		} catch (NumberFormatException e) {
			// Skip
		}
		return value;
	}

	/**
	 * Извлечение параметра типа Integer из PlaceRequest
	 * 
	 * @param request
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static Integer getInteger(PlaceRequest request, String key,
			Integer defaultValue) {
		Integer value = getInteger(request, key);
		return value != null ? value : defaultValue;
	}

}
