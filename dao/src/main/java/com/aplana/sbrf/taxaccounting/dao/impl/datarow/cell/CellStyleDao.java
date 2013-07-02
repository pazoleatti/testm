package com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormStyle;

import java.util.List;
import java.util.Map;

/**
 * интерфейс Dao для работы с стилями ячеек формы. Служит чтобы считывать и сохранять информацию о стилях ячеек
 * Предназначен для использования в {@link com.aplana.sbrf.taxaccounting.dao.FormDataDao}
 */
public interface CellStyleDao {
	/**
	 * заполнить список стилей ячеек для данной формы в список полученных DataRow
	 * @param formDataId идентификатор налоговой формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 * @param styles список стилей формы
	 * @return список столбцов формы
	 */
	void fillCellStyle(Long formDataId, Map<Long, DataRow<Cell>> rowIdMap, List<FormStyle> styles);
	/**
	 * Сохранить список редактируемых ячеек для заданной формы
	 * @param rowIdMap ключ - идентификатор строки в БД, значение - сама строка
	 */
	void saveCellStyle(Map<Long, DataRow<Cell>> rowIdMap);
}
