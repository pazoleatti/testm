package com.aplana.sbrf.taxaccounting.model;

/**
 * Столбец таблицы в объявлении налоговой формы
 * @author dsultanbekov
 */
public abstract class Column<T> {
	private Integer id;
	private String name;
	private int formId;
	private int order;
	private String alias;
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
