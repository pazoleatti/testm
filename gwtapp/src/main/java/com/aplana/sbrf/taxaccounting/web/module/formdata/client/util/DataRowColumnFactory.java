package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;

public class DataRowColumnFactory {
	
	Boolean isReadOnly = false;
	
	public Column<DataRow, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow> cellTable) {
		com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = null;
		if (col instanceof StringColumn) {
			StringColumn stringColumn = (StringColumn)col;
			if (isReadOnly || !col.isEditable()) {
				tableCol = new ReadOnlyTextColumn((StringColumn)col);
			} else {
				if (stringColumn.getDictionaryCode() != null) {
					tableCol = new EditStringDictionaryColumn(stringColumn);
				} else {
					tableCol = new EditTextColumn((StringColumn)col);
				}
			}
		} else if (col instanceof NumericColumn) {
			NumericColumn numericColumn = (NumericColumn) col;
			if (isReadOnly || !col.isEditable()) {
		    	tableCol = new ReadOnlyNumericColumn(numericColumn, cellTable);
		    } else {
				String dictionaryCode = numericColumn.getDictionaryCode();
				if(dictionaryCode !=null && !dictionaryCode.trim().isEmpty()) {
					tableCol = new EditNumericDictionaryColumn(numericColumn);
				}else{
					tableCol = new EditNumericColumn(numericColumn, cellTable);
				}
			}
		} else if (col instanceof DateColumn) {
		    if (isReadOnly || !col.isEditable()) {
		    	tableCol = new ReadOnlyDateColumn((DateColumn)col);
		    } else {
		    	tableCol = new EditDateColumn((DateColumn)col);
		    }
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
