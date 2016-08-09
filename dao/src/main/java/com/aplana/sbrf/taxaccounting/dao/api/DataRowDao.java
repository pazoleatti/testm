package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

import java.util.Collection;
import java.util.List;

/**
 * DAO для работы со строкам НФ.
 *
 * @author sgoryachkin
 *
 */
public interface DataRowDao {

	/**
	 * Удаляет точку восстановления, сделанную перед редактированием данных (бывший commit)
	 *
	 * @param formData
	 */
	void removeCheckPoint(FormData formData);

	/**
	 * Копирование строк из НФ-источника в НФ-приемник.
	 * Не копирует версию ручного ввода! Макеты источника и приемника должны быть одинаковыми/
	 * Перемещение данных происходит только в постоянном срезе
	 *
	 * @param formDataSourceId НФ источник
	 * @param formDataDestinationId НФ приемник
	 */
	void copyRows(long formDataSourceId, long formDataDestinationId);

	/**
	 * Создает версию ручного ввода, предварительно удалив из нее старые данные. Новые данные извлекаются из
	 * постоянного среза.
	 * @param formData
	 */
	void createManual(FormData formData);

	/**
	 * Создает точку восстановления при ручном редактировании. Работает как с обычной, так и с версией ручного ввода.
	 * (бывший createTemporary)
	 *
	 * @param formData
	 */
	void createCheckPoint(FormData formData);

	/**
	 * Метод возвращает строки сохраненного среза строк НФ.
	 *
	 */
	List<DataRow<Cell>> getRows(FormData formData, DataRowRange range);

	/**
	 * Метод возвращает строки сохраненного среза строк НФ со столбцами ссылочного типа или даты.
	 *
	 */
	List<DataRow<Cell>> getRowsRefColumnsOnly(FormData formData, DataRowRange range, boolean correctionDiff);

	/**
	 * Метод возвращает количество строк НФ
	 */
	int getRowCount(FormData formData);

    /**
     * Метод возвращает строки сохраненного среза строк НФ.
     *
     */
    List<DataRow<Cell>> getTempRows(FormData formData, DataRowRange range);

    /**
     * Метод возвращает количество строк НФ
     */
    int getTempRowCount(FormData formData);

    /**
     * Метод возвращает количество автонумеруемых строк
     */
    int getAutoNumerationRowCount(FormData formData);

	/**
	 * Вставляет строки начиная с указанного индекса. Выставленные в rows значения id и index игнорируются и
	 * перезаписываются.
	 * @param formData куда вставляем
	 * @param index начальный индекс, начиная с 1
	 * @param rows список новых строк
	 */
	void insertRows(FormData formData, int index, List<DataRow<Cell>> rows);

	/**
	 * Изменилось ли количество строк в табличной части до и после редактирования (до сохранения НФ).
	 * Только для автоматической версии (не ручного ввода)
	 *
	 * @param formData экземпляр НФ
	 * @return true - изменилось, false - не изменилось
	 */
	boolean isDataRowsCountChanged(FormData formData);

	/**
	 * Актуализирует список ссылок НФ на элементы справочника. Ссылки выставляются только для строк постоянного среза
	 * (автоматическая или версия ручного ввода)
	 * @param formData экземпляр НФ, ссылки которого требуется актуализировать
	 */
	void refreshRefBookLinks(FormData formData);

	/**
	 * Удаляем все строки
	 *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 */
	void removeRows(FormData formData);

	/**
	 * Удаляем все строки ручной версии
	 *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 */
	void removeAllManualRows(FormData formData);

	/**
	 * Удаляет строки в диапазоне индексов.
	 *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 * @param range диапазон удаляемых строк, индекс начинается с 1
	 */
	void removeRows(FormData formData, DataRowRange range);

	/**
	 * Удаляет указанные строки
	 *
	 * @param formData
	 * @param rows
	 */
	void removeRows(FormData formData, List<DataRow<Cell>> rows);

	/**
	 * Сохранить отсортированные строки без учета остальных изменений. Обновятся только значения атрибута ORD.
	 * Порядок задается последовательностью элементов в rows. Данный метод используется для пересортировки
	 * всех строк НФ.
	 */
	void reorderRows(FormData formData, List<DataRow<Cell>> rows);

	/**
	 * Откатывает все изменения и восстанавливает данные из контрольной точки
	 *
	 * @param formData
	 */
	void restoreCheckPoint(FormData formData);

	/**
	 * Заменяет существующие строки в БД на те, которые указаны в аргументе rows. Старые данные удаляются.
	 * Поля DataRow.index и DataRow.id не принимаются во внимание. Порядок следования выставляется согласно
	 * последовательности строк в rows. Id выставляется новый из последовательности "seq_form_data_nnn"
	 *
	 * @param formData
	 * @param rows
	 */
	void saveRows(FormData formData, List<DataRow<Cell>> rows);


    /**
     * Заменяет существующие строки в БД во временном(резервном) срезе на те, которые указаны в аргументе rows. Старые данные удаляются.
     * Поля DataRow.index и DataRow.id не принимаются во внимание. Порядок следования выставляется согласно
     * последовательности строк в rows. Id выставляется новый из последовательности "seq_form_data_nnn"
     *
     * @param formData
     * @param rows
     */
    void saveTempRows(FormData formData, List<DataRow<Cell>> rows);

    /**
     * Полнотекстовый поиск по данным налоговой формы
     *
     * @param formDataId идентификатор НФ
     * @param formTemplateId идентификатор шаблона НФ
     * @param range информация о выборке данных, с какой строки и сколько строк выбрать
     * @param key ключ для поиска
     * @param isCaseSensitive чувствительность к регистру
     * @param manual ручной ввод
     * @return Set<FormDataSearchResult> - Набор из номера столбца, строки, и самой найденной подстроки
     */
    PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive, boolean manual, boolean correctionDiff);

	/**
	 * Обновляет существующие строки НФ
	 */
	void updateRows(FormData formData, Collection<DataRow<Cell>> rows);

}
