package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.util.Ordered;

/**
 * Скрипт 
 */
public class Script implements Ordered, Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String body;
	
	/**
	 * Порядок исполнения
	 */
	private int order;	
	
	/**
	 * Имя скрипта
	 */
	private String name;

	/**
	 * Условие, которое должно выполняться, чтобы скрипт можно было выполнить для строки
	 */	
	private String condition;
	
	/**
	 * Признак того, что скрипт нужно выполнять для отдельных строк, а не для формы в целом
	 */
	private boolean rowScript;
	
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

	/**
	 * Получить идентификатор скрипта
	 * @return идентификатор скрипта
	 */
	public int getId() {
		return id;
	}

	/**
	 * Задать идентификатор скрипта
	 * @param id идентификатор скрипта
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	
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

	public boolean isRowScript() {
		return rowScript;
	}

	public void setRowScript(boolean rowScript) {
		this.rowScript = rowScript;
	}
}
