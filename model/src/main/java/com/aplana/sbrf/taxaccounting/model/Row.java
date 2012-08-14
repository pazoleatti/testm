package com.aplana.sbrf.taxaccounting.model;

/**
 * Строка в объявлении таблицы
 * @author dsultanbekov
 */
public class Row {
	private Integer id;
	private int formId;
	private int order;
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
}
