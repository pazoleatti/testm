package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.Column;

/**
 * Контекст колонки
 *@author Eugene Stetsenko
 */
public class ColumnContext {
	public static enum Mode {
		EDIT_MODE,
		READONLY_MODE,
		DEFAULT_MODE
	}

	Mode mode = Mode.DEFAULT_MODE;
	Column column;

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

}
