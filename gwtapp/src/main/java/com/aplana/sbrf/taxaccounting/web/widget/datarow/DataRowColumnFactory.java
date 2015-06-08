package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;

import java.util.Date;

public class DataRowColumnFactory {
	private boolean readOnly;
	private boolean superEditMode;
    private boolean lockMode;
	private Date startDate;
	private Date endDate;
    private Long formDataId;

	public Column<DataRow<Cell>, ?> createTableColumn(com.aplana.sbrf.taxaccounting.model.Column col, AbstractCellTable<DataRow<Cell>> cellTable) {
		ColumnContext columnContext = new ColumnContext();
		if (lockMode || readOnly) {
			columnContext.setMode(ColumnContext.Mode.READONLY_MODE);
		} else {
			if (superEditMode){
				columnContext.setMode(ColumnContext.Mode.SUPER_EDIT_MODE);
			} else {
				columnContext.setMode(ColumnContext.Mode.NORMAL_EDIT_MODE);
			}
		}
        columnContext.setFormDataId(formDataId);
		columnContext.setDateRange(startDate, endDate);
		Column<DataRow<Cell>, ?> uiColumn;
		switch (col.getColumnType()) {
			case STRING:
				StringColumn stringColumn = (StringColumn)col;
				columnContext.setColumn(stringColumn);
				uiColumn = new EditTextColumn((StringColumn)col, columnContext);
				break;
			case NUMBER:
				NumericColumn numericColumn = (NumericColumn) col;
				columnContext.setColumn(numericColumn);
				uiColumn = new EditNumericColumn(numericColumn, columnContext);
				break;
			case DATE:
				DateColumn dateColumn = (DateColumn) col;
				columnContext.setColumn(dateColumn);
				uiColumn = new EditDateColumn(dateColumn, columnContext);
				break;
			case REFBOOK:
				RefBookColumn refBookColumn = (RefBookColumn) col;
				columnContext.setColumn(refBookColumn);
				uiColumn = new RefBookUiColumn(refBookColumn, columnContext);
				break;
			case REFERENCE:
				ReferenceColumn referenceColumn = (ReferenceColumn) col;
				columnContext.setColumn(referenceColumn);
				uiColumn = new ReferenceUiColumn(referenceColumn, columnContext);
				break;
			case AUTO:
				AutoNumerationColumn autoNumerationColumn = (AutoNumerationColumn) col;
				columnContext.setColumn(autoNumerationColumn);
				uiColumn = new AutoNumerationUiColumn(autoNumerationColumn);
				break;
			default:
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

    public void setLockMode(boolean lockMode) {
        this.lockMode = lockMode;
    }

    public void setDateRange(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }
}
