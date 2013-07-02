package com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.Map;

/**
 * интерфейс Dao для работы с редактируемыми ячейками формы. Служит чтобы считывать и сохранять информацию о редактируемых ячейках
 * Предназначен для использования в {@link com.aplana.sbrf.taxaccounting.dao.FormDataDao}
 */
public interface CellEditableDao {
	/**
	 * заполнить список редактируемых ячеек для данной формы в список полученных DataRow
	 * @param formDataId идентификатор налоговой формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 * @return список столбцов формы
	 */
	void fillCellEditable(Long formDataId, Map<Long, DataRow<Cell>> rowIdMap);
	/**
	 * Сохранить список редактируемых ячеек для заданной формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 */
	void saveCellEditable(Map<Long, DataRow<Cell>> rowIdMap);
}
