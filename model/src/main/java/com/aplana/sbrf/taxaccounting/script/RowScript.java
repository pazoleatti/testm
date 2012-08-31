package com.aplana.sbrf.taxaccounting.script;

/**
 * Скрипт, выполняющийся для отдельной строки формы
 */
public class RowScript extends Script {
	/**
	 * Имя скрипта
	 */
	private String name;
	/**
	 * Условие, которое должно выполняться, чтобы скрипт можно было выполнить для строки
	 */
	private String condition;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
}