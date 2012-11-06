package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;

public class DataRowColumnFactory {
	public Column<DataRow, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow> cellTable) {
		com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = null;
		if (col instanceof StringColumn) {
			StringColumn stringColumn = (StringColumn)col;
			if (stringColumn.getDictionaryCode() != null) {
				tableCol = new EditStringDictionaryColumn(stringColumn);
			} else {
				tableCol = new EditTextColumn((StringColumn)col);	
			}
		} else if (col instanceof NumericColumn) {
			tableCol = new EditNumericColumn((NumericColumn)col, cellTable);
		} else if (col instanceof DateColumn) {
			tableCol = new EditDateColumn((DateColumn)col);
		}
		return tableCol;
	}
}
