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
	
	public DataRow appendDataRow(String rowAlias) {
		DataRow row = new DataRow(rowAlias, form.getColumns());
		dataRows.add(row);
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
