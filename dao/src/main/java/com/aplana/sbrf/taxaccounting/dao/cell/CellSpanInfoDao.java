package com.aplana.sbrf.taxaccounting.dao.cell;

import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.Map;

/**
 * интерфейс Dao для работы с rowspan и colspan для ячеек формы.
 * Служит чтобы считывать и сохранять информацию rowspan и colspan для ячеек
 * Предназначен для использования в {@link com.aplana.sbrf.taxaccounting.dao.FormDataDao}
 */
public interface CellSpanInfoDao {
	/**
	 * заполнить список rowspan и colspan для данной формы в список полученных DataRow
	 * @param formDataId идентификатор налоговой формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 * @return список столбцов формы
	 */
	void fillCellSpanInfo(Long formDataId, Map<Long, DataRow> rowIdMap);
	/**
	 * Сохранить список редактируемых ячеек для заданной формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 */
	void saveCellSpanInfo(Map<Long, DataRow> rowIdMap);
}
