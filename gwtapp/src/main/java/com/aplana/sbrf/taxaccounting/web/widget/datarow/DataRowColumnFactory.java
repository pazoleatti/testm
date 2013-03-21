package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;

public class DataRowColumnFactory {
	private Boolean isReadOnly = false;
	private Boolean isEditOnly = false;

	public Column<DataRow, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow> cellTable) {
		ColumnContext columnContext = new ColumnContext();
		if (isEditOnly && !isReadOnly) {
			columnContext.setMode(ColumnContext.Mode.EDIT_MODE);
		} else if (isReadOnly && !isEditOnly) {
			columnContext.setMode(ColumnContext.Mode.READONLY_MODE);
		} else {
			columnContext.setMode(ColumnContext.Mode.DEFAULT_MODE);
		}
		com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = null;
		if (col instanceof StringColumn) {
			StringColumn stringColumn = (StringColumn)col;
			columnContext.setColumn(stringColumn);
			if (stringColumn.getDictionaryCode() != null) {
				tableCol = new EditStringDictionaryColumn(stringColumn, columnContext);
			} else {
				tableCol = new EditTextColumn((StringColumn)col, columnContext);
			}
		} else if (col instanceof NumericColumn) {
			NumericColumn numericColumn = (NumericColumn) col;
			columnContext.setColumn(numericColumn);
			String dictionaryCode = numericColumn.getDictionaryCode();
			if(dictionaryCode !=null && !dictionaryCode.trim().isEmpty()) {
				tableCol = new EditNumericDictionaryColumn(numericColumn, columnContext);
			}else{
				tableCol = new EditNumericColumn(numericColumn, columnContext);
			}
		} else if (col instanceof DateColumn) {
			DateColumn dateColumn = (DateColumn) col;
			columnContext.setColumn(dateColumn);
	        tableCol = new EditDateColumn(dateColumn, columnContext);

		}
		return tableCol;
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
