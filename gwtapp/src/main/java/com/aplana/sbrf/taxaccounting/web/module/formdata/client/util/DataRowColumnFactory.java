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
			if (stringColumn.getDictionaryCode() != null) {
				if (isReadOnly || !col.isEditable()) {
					tableCol = new ReadOnlyTextColumn((StringColumn)col);//new EditStringDictionaryColumn(stringColumn);
				} else {
					tableCol = new EditStringDictionaryColumn(stringColumn);
				}
			} else {
				if (isReadOnly || !col.isEditable()) {
					tableCol = new ReadOnlyTextColumn((StringColumn)col);
			    } else {
			    	tableCol = new EditTextColumn((StringColumn)col);
			    }
			}
		} else if (col instanceof NumericColumn) {
		    if (isReadOnly || !col.isEditable()) {
		    	tableCol = new ReadOnlyNumericColumn((NumericColumn)col, cellTable);
		    } else {
		    	tableCol = new EditNumericColumn((NumericColumn)col, cellTable);
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
