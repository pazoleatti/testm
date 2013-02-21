package com.aplana.sbrf.taxaccounting.model;


/**
 * Запись в таблице cell_editable
 */
public class CellEditable {
	private Long rowId;
	private Integer columnId;

	public CellEditable(Long rowId, Integer columnId) {
		this.rowId = rowId;
		this.columnId = columnId;
	}

	public Long getRowId() {
		return rowId;
	}

	public void setRowId(Long rowId) {
		this.rowId = rowId;
	}

	public Integer getColumnId() {
		return columnId;
	}
}
