package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;

public class DataRowEditableCellUtils {
	
	/**
	 * Ячейка в режиме чтений когда:
	 * READONLY_MODE
	 * или
	 * DEFAULT_MODE и редактирование ячейки не допустимо
	 * 
	 * в остальных случаях редактирование доступно
	 * 
	 * @param columnContext
	 * @param cell
	 * @return
	 */
	public static boolean editMode(ColumnContext columnContext, AbstractCell cell){
		boolean editableCell;
		if (cell instanceof Cell) {
			editableCell = ((Cell) cell).isEditable();
		} else {
			editableCell = true;
		}
		if ((columnContext.getMode() == ColumnContext.Mode.READONLY_MODE)
				|| (columnContext.getMode() == ColumnContext.Mode.NORMAL_EDIT_MODE && !editableCell)) {
			return false;
		} else {
			return true;
		}
	}

}
