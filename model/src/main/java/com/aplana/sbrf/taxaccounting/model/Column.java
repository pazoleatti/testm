package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;


/**
 * Столбец таблицы в объявлении налоговой формы
 * @author dsultanbekov
 */
public abstract class Column implements Ordered, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer id = null;
	private String name;
	private String alias;
	private int width;
	private boolean editable;
	private boolean mandatory;
	private int order;
	private String groupName;
	
	/**
	 * Идентификатор столбца в БД
	 * Если значение < 0, то считается, что столбец новый и при его сохранении будет сгенерирован новый идентификатор
	 * @return идентификатор столбца
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Задать значение идентификатора столбца. Если id==null, то столбец считается не сохраненным в БД
	 * и при сохранении формы добавится в БД.
	 *
	 * @param id значение идентификатора, для новых столбцов задавать не нужно, т.к. по умолчанию null.
	 */
	public void setId(Integer id) {
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
