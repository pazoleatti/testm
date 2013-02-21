package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.CellEditable;

import java.util.List;

/**
 * интерфейс Dao для работы с редактируемыми ячейками формы. Служит чтобы считывать и сохранять информацию о редактируемых ячейках
 * Предназначен для использования в {@link FormDataDao}
 */
public interface CellEditableDao {
	/**
	 * Получить список редактируемых ячеек, входящих в заданную форму
	 * @param formDataId идентификатор формы
	 * @return список столбцов формы
	 */
	List<CellEditable> getFormCellEditable(Long formDataId);
	/**
	 * Сохранить список редактируемых ячеек для заданной формы
	 * @param cellEditableList значения из таблицы cell_editable для конкретной формы
	 */
	void saveFormEditableCells(final List<CellEditable> cellEditableList);
}
