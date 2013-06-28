package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

/**
 * DAO для работы со строкам НФ. При редактировании состояние формы делится на 2
 * среза - Сохранненый (Saved) - Редактируемый (Edited)
 * 
 * Все действия по изменению строк НФ происходят с редактируемым срезом. После
 * сохранения сохраненный срез удаляется, а редактируемый становится сохраненным
 * При отмене редактируемый срез удаляется
 * 
 * Если форма в данный момент никем не редактируется (после save или cancel
 * изменений небыло) значит редактируемое и сохраненное состояние совпадают.
 * 
 * @author sgoryachkin
 * 
 */
public interface DataRowDao {

	/*
	 * Методы для работы с сохраненным срезом формы
	 */

	/**
	 * Метод получает строки сохранненого состояния
	 * 
	 */
	List<DataRow<Cell>> getSavedRows(FormData fd, DataRowFilter filter,
			DataRowRange range);
	
	int getSavedSize(FormData fd, DataRowFilter filter,
			DataRowRange range);

	/*
	 * Методы для работы с редактируемым срезом формы
	 */

	/**
	 * Метод получает строки редактируемого в данный момент состояния формы.
	 * 
	 */
	List<DataRow<Cell>> getRows(FormData fd, DataRowFilter filter,
			DataRowRange range);
	

	
	int getSize(FormData fd, DataRowFilter filter,
			DataRowRange range);

	/**
	 * Обновляет строки НФ
	 * 
	 * @param fd
	 * @param row
	 */
	void updateRows(FormData fd, List<DataRow<Cell>> rows);

	void removeRow(FormData fd, DataRow<Cell> row);

	void removeRow(FormData fd, int index);

	DataRow<Cell> insertRow(FormData fd, int index, DataRow<Cell> row);

	DataRow<Cell> insertRowAfter(FormData fd, DataRow<Cell> afterRow, DataRow<Cell> row);

	/*
	 * Сохранение/отмена
	 */

	void save(FormData fd);

	void cancel(FormData fd);

}
