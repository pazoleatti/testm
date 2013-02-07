package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Вид декларации.
 * @author dsultanbekov
 */
public class DeclarationType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private TaxType taxType;
	private String name;

	/**
	 * Получить идентификатор
	 * @return идентфикатор
	 */
	public int getId() {
		return id;
	}

	/**
	 * Задать идентификатор
	 * @param id значение идентификатора
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Получить вид налога
	 * @return вид налога
	 */
	public TaxType getTaxType() {
		return taxType;
	}
	
	/**
	 *  Задать вид налога
	 * @param taxType вид налога
	 */
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
	
	/**
	 * Получить название вида декларации
	 * @return название вида декларации
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать название вида декларации
	 * @param name название вида декларации
	 */
	public void setName(String name) {
		this.name = name;
	}
}
