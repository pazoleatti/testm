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

import java.util.Date;

public class DataRowColumnFactory {
	private boolean readOnly;
	private boolean superEditMode;
	private Date startDate;
	private Date endDate;

	public Column<DataRow<Cell>, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow<Cell>> cellTable) {
		ColumnContext columnContext = new ColumnContext();
		if (readOnly) {
			columnContext.setMode(ColumnContext.Mode.READONLY_MODE);
		} else {
			if (superEditMode){
				columnContext.setMode(ColumnContext.Mode.SUPER_EDIT_MODE);
			} else {
				columnContext.setMode(ColumnContext.Mode.NORMAL_EDIT_MODE);
			}
		}
		columnContext.setDateRange(startDate, endDate);
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


	/**
	 * Устанавливает флаг недоступности редактирования
	 * 
	 * @param readOnly
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Устанавливает флаг доступности суперредактирования всех ячеек в режиме редактирования
	 * 
	 * @param editOnly
	 */
	public void setSuperEditMode(boolean editOnly) {
		this.superEditMode = editOnly;
	}

	public void setDateRange(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
