package com.aplana.sbrf.taxaccounting.dao.api;

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
	 * @param fd - FormData со строками которой идет работа
	 * @param handler
	 * @param filter
	 * @param range
	 */
	void getSavedRows(FormData fd, DataRowHandler handler, DataRowFilter filter,
			DataRowRange range);
	
	int getSavedSize(FormData fd, DataRowFilter filter,
			DataRowRange range);

	/*
	 * Методы для работы с редактируемым срезом формы
	 */

	/**
	 * Метод получает строки редактируемого в данный момент состояния формы.
	 * 
	 * @param fd - FormData со строками которой идет работа
	 * @param handler
	 * @param filter фильтр (возможно значение null)
	 * @param range диапазон (возможно значение null)
	 */
	void getRows(FormData fd, DataRowHandler handler, DataRowFilter filter,
			DataRowRange range);
	
	/**
	 * Метод получает строку редактируемого в данный момент состояния формы.
	 * 
	 * @param fd - FormData со строками которой идет работа
	 * @param index
	 * @param filter
	 * @param range
	 * @return
	 */
	DataRow<Cell> getRow(FormData fd, int index, DataRowFilter filter,
			DataRowRange range);
	
	int getSize(FormData fd, DataRowFilter filter,
			DataRowRange range);

	/**
	 * Обновляет существующую строку НФ
	 * 
	 * @param fd
	 * @param row
	 */
	void updateRow(FormData fd, DataRow<Cell> row);

	void removeRow(FormData fd, DataRow<Cell> row);

	void removeRow(FormData fd, int index);

	DataRow<Cell> addRow(FormData fd, int index, DataRow<Cell> rowTemplate);

	DataRow<Cell> addRow(FormData fd, DataRow<Cell> rowTemplate);

	DataRow<Cell> addRowAfter(FormData fd, DataRow<Cell> afterRow,
			DataRow<Cell> rowTemplate);

	DataRow<Cell> addRowBefore(FormData fd, DataRow<Cell> beforeRow,
			DataRow<Cell> rowTemplate);

	/*
	 * Сохранение/отмена
	 */

	void save(FormData fd);

	void cancel(FormData fd);

}
