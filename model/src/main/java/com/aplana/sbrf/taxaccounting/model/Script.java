package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;


/**
 * Скрипт 
 */
public class Script implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String body;

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

	/**
	 * @return если возвращает true, то скрипт выполняется для каждой строки формы,
	 * иначе для все формы в целом 1 раз
	 */
	public boolean isRowScript() {
		return rowScript;
	}

	/**
	 * @param rowScript признак того что скрипт выполняется для каждой строки или для всей формы в целов.
	 *                  если true, то для каждой строки, если false то для всей формы в целом.
	 */
	public void setRowScript(boolean rowScript) {
		this.rowScript = rowScript;
	}
}
