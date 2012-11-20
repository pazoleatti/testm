package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;

public class DataRowColumnFactory {
	
	Boolean isReadOnly = true;
	
	public Column<DataRow, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow> cellTable) {
		com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = null;
		if (col instanceof StringColumn) {
			StringColumn stringColumn = (StringColumn)col;
			if (stringColumn.getDictionaryCode() != null) {
				if (isReadOnly) {
					tableCol = new TextColumn((StringColumn)col, true);//new EditStringDictionaryColumn(stringColumn);
				} else {
					tableCol = new EditStringDictionaryColumn(stringColumn);
				}
			} else {
				tableCol = new TextColumn((StringColumn)col, isReadOnly);	
			}
		} else if (col instanceof NumericColumn) {
			tableCol = new EditNumericColumn((NumericColumn)col, cellTable, isReadOnly);
		} else if (col instanceof DateColumn) {
			tableCol = new SimpleDateColumn((DateColumn)col, isReadOnly);
		}
		return tableCol;
	}
	
	public Boolean isReadOnly() {
		return isReadOnly;
	}
	
	public void setReadOnly(Boolean readOnly) {
		this.isReadOnly = readOnly;
	}
}
