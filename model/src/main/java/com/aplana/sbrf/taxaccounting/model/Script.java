package com.aplana.sbrf.taxaccounting.model;

/**
 * Скрипт 
 */
public class Script {
	private String body;

	/**
	 * Получить исходный код скрипта 
	 * @return исходный код скрипта в виде строки
	 */
	
	public String getBody() {
		return body;
	}

	/**
	 * Задать код скрипта
	 * @param body исходный код скрипта в виде строки
	 */
	public void setBody(String body) {
		this.body = body;
	}
}
