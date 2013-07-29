package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;

public class DataRowColumnFactory {
	private Boolean isReadOnly = false;
	private Boolean isEditOnly = false;

	public Column<DataRow<Cell>, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow<Cell>> cellTable) {
		ColumnContext columnContext = new ColumnContext();
		if (isEditOnly && !isReadOnly) {
			columnContext.setMode(ColumnContext.Mode.EDIT_MODE);
		} else if (isReadOnly && !isEditOnly) {
			columnContext.setMode(ColumnContext.Mode.READONLY_MODE);
		} else {
			columnContext.setMode(ColumnContext.Mode.DEFAULT_MODE);
		}
		Column<DataRow<Cell>, ?> uiColumn = null;
		if (col instanceof StringColumn) {
			StringColumn stringColumn = (StringColumn)col;
			columnContext.setColumn(stringColumn);
			uiColumn = new EditTextColumn((StringColumn)col, columnContext);
		} else if (col instanceof NumericColumn) {
			NumericColumn numericColumn = (NumericColumn) col;
			columnContext.setColumn(numericColumn);
			uiColumn = new EditNumericColumn(numericColumn, columnContext);
		} else if (col instanceof DateColumn) {
			DateColumn dateColumn = (DateColumn) col;
			columnContext.setColumn(dateColumn);
	        uiColumn = new EditDateColumn(dateColumn, columnContext);
		} else if (col instanceof RefBookColumn){
			RefBookColumn refBookColumn = (RefBookColumn) col;
			columnContext.setColumn(refBookColumn);
			uiColumn = new RefBookUiColumn(refBookColumn, columnContext);
		} else {
			throw new IllegalArgumentException();
		}
		return uiColumn;
	}

	public Boolean isReadOnly() {
		return isReadOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.isReadOnly = readOnly;
	}

	public void setEditOnly(Boolean editOnly) {
		this.isEditOnly = editOnly;
	}
}
