package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.Map;

/**
 * интерфейс Dao для работы с редактируемыми ячейками формы. Служит чтобы считывать и сохранять информацию о редактируемых ячейках
 * Предназначен для использования в {@link FormDataDao}
 */
public interface CellEditableDao {
	/**
	 * заполнить список редактируемых ячеек для данной формы в список полученных DataRow
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 * @return список столбцов формы
	 */
	void fillCellEditable(Long formDataId, Map<Long, DataRow> rowIdMap);
	/**
	 * Сохранить список редактируемых ячеек для заданной формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 */
	void saveCellEditable(Map<Long, DataRow> rowIdMap);
}
