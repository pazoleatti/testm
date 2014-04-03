package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.Column;

import java.util.Date;

/**
 * Контекст колонки
 *@author Eugene Stetsenko
 */
public class ColumnContext {
	public static enum Mode {
		SUPER_EDIT_MODE,
		READONLY_MODE,
		NORMAL_EDIT_MODE
	}

	Mode mode = Mode.NORMAL_EDIT_MODE;
	Column column;
	Date startDate;
	Date endDate;

    Long formDataId;

	public ColumnContext() {
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	public Column getColumn() {
		return this.column;
	}

	public void setDateRange(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }
}
