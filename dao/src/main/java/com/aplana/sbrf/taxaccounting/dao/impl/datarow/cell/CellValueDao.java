package com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.Map;

/**
 * интерфейс Dao для работы со значениям ячеек формы. Служит чтобы считывать и сохранять информацию о значениях в ячейках
 * Предназначен для использования в {@link com.aplana.sbrf.taxaccounting.dao.FormDataDao}
 */
public interface CellValueDao {

	/**
	 * Сохранить список редактируемых ячеек для заданной формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 */
	void saveCellValue(Map<Long, DataRow<Cell>> rowIdMap);
}
