package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание налоговой формы
 * @author dsultanbekov
 */
public class Form {
	private Integer id;
	private final List<Column> columns = new ArrayList<Column>();
	private FormType type;
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setType(FormType type) {
		this.type = type;
	}
	public FormType getType() {
		return type;
	}
	public Column getColumn(int columnId) {
		for (Column col: columns) {
			if (col.getId() == columnId) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnId: " + columnId);
	}
	public Column getColumn(String columnAlias) {
		if (columnAlias == null) {
			throw new NullPointerException("Column alias cannot be null");
		}
		for (Column col: columns) {
			if (columnAlias.equals(col.getAlias())) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnAlias: " + columnAlias);
	}
}
