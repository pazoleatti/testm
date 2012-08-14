package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Строка данных отчётной формы
 * @author dsultanbekov
 */
public class DataRow {
	private final Map<String, BigDecimal> data;
	private final Row row;
	
	public DataRow(Row row, List<Column> columns) {
		this.row = row;
		this.data = new HashMap<String, BigDecimal>(columns.size());
	}
	
	public BigDecimal getData(String columnAlias) {
		return data.get(columnAlias);
	}
	
	public void setData(String columnAlias, BigDecimal value) {
		if (value == null) {
			data.remove(columnAlias);
		} else {
			data.put(columnAlias, value);
		}
	}

	public Row getRow() {
		return row;
	}
}
