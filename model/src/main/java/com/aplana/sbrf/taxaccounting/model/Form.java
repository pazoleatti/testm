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
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Column> getColumns() {
		return columns;
	}
}
