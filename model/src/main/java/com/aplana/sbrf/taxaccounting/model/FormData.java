package com.aplana.sbrf.taxaccounting.model;

import java.util.List;

/**
 * Данные по отчётной форме
 * @author dsultanbekov
 */
public class FormData {
	private Long id;
	private final Form form;
	private List<DataRow> dataRows;
	
	public FormData(Long id, Form form) {
		this.id = id;
		this.form = form;
		List<Column> columns = form.getColumns();
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
