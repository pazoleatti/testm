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
	private final List<DataRow> dataRows;
	
	public FormData(Long id, Form form) {
		this.id = id;
		this.form = form;
		List<Row> rows = form.getRows();
		List<Column> columns = form.getColumns();
		this.dataRows = new ArrayList<DataRow>(rows.size());
		for (Row row: rows) {
			dataRows.add(new DataRow(row, columns));
		}
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
}
