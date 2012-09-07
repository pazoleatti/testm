package com.aplana.sbrf.taxaccounting.model;

/**
 * Скрипт, вычисляющий значение конкретной ячейки
 */
public class ValueScript extends Script {
	/**
	 * Условие, которое должно выполняться, чтобы скрипт можно было выполнить для строки
	 */	
	private String condition;

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
}
