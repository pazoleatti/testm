package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.Collection;
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
	 * Метод получает строки сохранненого среза строк НФ.
	 * 
	 */
	List<DataRow<Cell>> getSavedRows(FormData fd, DataRowFilter filter,
			DataRowRange range);
	
	/**
	 * Метод получает количество строк сохранненого среза.
	 * 
	 * @param fd
	 * @param filter
	 * @return
	 */
	int getSavedSize(FormData fd, DataRowFilter filter);

	/*
	 * Методы для работы с редактируемым срезом формы
	 */

	/**
	 * Метод получает строки редактируемого в данный момент среза строк НФ.
	 * 
	 */
	List<DataRow<Cell>> getRows(FormData fd, DataRowFilter filter,
			DataRowRange range);
	

	
	/**
	 * Метод получает количество строк редактируемого среза.
	 * 
	 * @param fd
	 * @param filter
	 * @return
	 */
	int getSize(FormData fd, DataRowFilter filter);

	/**
	 * Обновляет строки НФ. Строки остаются приаттаченными к текущему срезу НФ
	 * При этом поле id у DataRow может быть обновлено.
	 * 
	 * @param fd
	 * @param row
	 */
	void updateRows(FormData fd, Collection<DataRow<Cell>> rows);

	/**
	 * Удалет строки. При этом используется иденитфикатор DataRow.id 
	 * Действие применяется к временному срезу строк
	 * 
	 * @param fd
	 * @param rows
	 */
	void removeRows(FormData fd, List<DataRow<Cell>> rows);

	/**
	 * Удаляет строки в диапазоне индексов. (Индексы от 1)
	 * Действие применяется к временному срезу строк
	 * 
	 * @param fd
	 * @param idxFrom
	 * @param idxTo
	 */
	void removeRows(FormData fd, int idxFrom, int idxTo);
	
	
	/**
	 * Удаляем все строки
	 * Действие применяется к временному срезу строк
	 * 
	 * @param fd
	 */
	void removeRows(FormData fd);
	
	
	/**
	 * Сохраняет все строки во временном срезе формы, при этом сохраняется порядок, и 
	 * удаляются все существующие строки. Фактически метод ведет себя как старый способ сохранения формы.
	 * Поля DataRow.index и DataRow.id не принимаются во внимание. 
	 * 
	 * @param fd
	 * @param rows
	 */
	void saveRows(FormData fd, List<DataRow<Cell>> rows);

	void insertRows(FormData fd, int index, List<DataRow<Cell>> rows);

	void insertRows(FormData fd, DataRow<Cell> afterRow, List<DataRow<Cell>> rows);

	/*
	 * Сохранение/отмена
	 */

	/**
	 * Делает временный срез строк формы - постоянным.
	 * 
	 * @param fd
	 */
	void commit(FormData fd);

	/**
	 * Откатывает временный срез формы к постоянному.
	 * 
	 * @param fd
	 */
	void rollback(FormData fd);

}
