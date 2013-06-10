package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

/**
 * DAO для работы со строкам НФ.
 * При редактировании состояние формы делится на 2 среза
 * - Сохранненый (Saved)
 * - Редактируемый (Edited)
 * 
 * Все действия по изменению строк НФ происходят с редактируемым срезом.
 * После сохранения сохраненный срез удаляется, а редактируемый становится сохраненным
 * При отмене редактируемый срез удаляется
 * 
 * @author sgoryachkin
 *
 */
public interface DataRowDao {
	
	/*
	 * 
	 * Методы для работы с сохраненным срезом формы
	 * 
	 */

	void getSavedRows(Long id, DataRowHandler handler, DataRowFilter filter, DataRowRange range);
	
	/*
	 * 
	 * Методы для работы с редактируемым срезом формы
	 * 
	 */
	
	void getRows(Long id, DataRowHandler handler, DataRowFilter filter, DataRowRange range);
	
	void getRow(Long id, int index);
	
	void updateRow(Long id, DataRow<Cell> row);
	
	void removeRow(Long id, DataRow<Cell> row);
	
	void removeRow(Long id, int index);
	
	DataRow<Cell> addRow(Long id, int index, DataRow<Cell> rowTemplate);
	
	DataRow<Cell> addRow(Long id, DataRow<Cell> rowTemplate);
	
	DataRow<Cell> addRowAfter(Long id, DataRow<Cell> afterRow, DataRow<Cell> rowTemplate);
	
	DataRow<Cell> addRowBefore(Long id, DataRow<Cell> beforeRow, DataRow<Cell> rowTemplate);
	
	/*
	 * 
	 * Сохранение/отмена
	 * 
	 */
	
	void save(Long id);
	
	void cancel(Long id);
		
}
