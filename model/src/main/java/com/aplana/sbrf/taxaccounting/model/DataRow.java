package com.aplana.sbrf.taxaccounting.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Строка данных отчётной формы
 * @author dsultanbekov
 */
public class DataRow {
	private final Map<String, Object> data;
	private String alias;

	public DataRow(String alias, List<Column<?>> columns) {
		this.alias = alias;
		data = new HashMap<String, Object>(columns.size());
		for (Column<?> col: columns) {
			data.put(col.getAlias(), null);
		}
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Object getColumnValue(String columnAlias) {
		return data.get(columnAlias);
	}

	public void setColumnValue(String columnAlias, Object value) {
		if (value == null) {
			data.remove(columnAlias);
		} else {
			data.put(columnAlias, value);
		}
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String code) {
		this.alias = code;
	}
}
