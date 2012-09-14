package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Данные по отчётной форме
 * @author dsultanbekov
 */
public class FormData {
	private Long id;
	private final Form form;
	private final List<DataRow> dataRows = new ArrayList<DataRow>();
	
	public FormData(Long id, Form form) {
		this.id = id;
		this.form = form;
	}
	
	public FormData(Form form) {
		this(null, form);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Form getForm() {
		return form;
	}

	public List<DataRow> getDataRows() {
		return dataRows;
	}

	/**
	 * Добавляет строку в таблицу данных
	 * Каждая строка обязательно должна содержать уникальный алиас, для возможности
	 * идентификации её в скриптах
	 * @param rowAlias значение, задающее алиас.
	 * в большинстве случае должен быть строкой, но для удобства написания скриптов, принимает Object.
	 * Значением алиаса будет результат операции <code>rowAlias.toString()</code>
	 * @return добавленная строка с установленным алиасом
	 */
	public DataRow appendDataRow(Object rowAlias) {
		DataRow row = new DataRow(rowAlias.toString(), form);		
		dataRows.add(row);
		row.setOrder(dataRows.size() + 1);
		return row;
	}
	
	public DataRow getDataRow(String rowAlias) {
		if (rowAlias == null) {
			throw new NullPointerException("Row alias cannot be null");
		}
		for (DataRow row: dataRows) {
			if (rowAlias.equals(row.getAlias())) {
				return row;
			}
		}
		throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
	}
}
