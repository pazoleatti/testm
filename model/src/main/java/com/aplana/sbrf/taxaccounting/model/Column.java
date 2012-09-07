package com.aplana.sbrf.taxaccounting.model;

/**
 * Столбец таблицы в объявлении налоговой формы
 * @author dsultanbekov
 */
public abstract class Column {
	private int id;
	private String name;
	private String alias;
	private int width;
	private boolean editable;
	private boolean mandatory;
	private ValueScript valueScript;
	
	/**
	 * Идентификатор столбца в БД
	 * Если значение < 0, то считается, что столбец новый и при его сохранении будет сгенерирован новый идентификатор
	 * @return идентификатор столбца
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Задать значение идентификатора столбца
	 * @param id значение идентификатора, для новых столбцов нужно задавать отрицательные значения
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public ValueScript getValueScript() {
		return valueScript;
	}

	public void setValueScript(ValueScript valueScript) {
		this.valueScript = valueScript;
	}
}
