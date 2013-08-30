package com.aplana.sbrf.taxaccounting.web.widget.cell;

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
	 * @param editableCell
	 * @return
	 */
	public static boolean editMode(ColumnContext columnContext, boolean editableCell){
		if ((columnContext.getMode() == ColumnContext.Mode.READONLY_MODE)
				|| (columnContext.getMode() == ColumnContext.Mode.DEFAULT_MODE && !editableCell)) {
			return false;
		} else {
			return true;
		}
	}

}
