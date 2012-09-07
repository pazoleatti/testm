package com.aplana.sbrf.taxaccounting.model;

/**
 * Скрипт, выполняющийся для отдельной строки формы
 */
public class RowScript extends ValueScript {
	/**
	 * Имя скрипта
	 */
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}