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
	private String code;
	private int order;
	
	public DataRow(String code, List<Column<?>> columns) {
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
