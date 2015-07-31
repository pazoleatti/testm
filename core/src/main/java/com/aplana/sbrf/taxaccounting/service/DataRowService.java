package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

import java.util.List;

public interface DataRowService {
	
	/**
	 * Получение страницы с набором строк НФ
	 */
	PagingResult<DataRow<Cell>> getDataRows(long formDataId, DataRowRange range, boolean saved, boolean manual);

    /**
     * Получение сохраненных строк НФ
     */
    List<DataRow<Cell>> getSavedRows(FormData formData);
	
	/**
	 * Получени количество строк НФ
	 */
	int getRowCount(long formDataId, boolean saved, boolean manual);
	
	/**
	 * Обновление набора строк во временном срезе НФ
     */
	void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows, boolean manual);

    /**
     * Сохранение строк во временном срезе НФ
     */
    void saveRows(FormData formData, List<DataRow<Cell>> dataRows);

    /**
     * Поиск по налоговой форме,
     * ищутся совпадения и выдается номер строки и столбца
     * на форме
     *
     * @param formDataId
     * @param range информация о выборке данных, с какой строки и сколько строк выбрать
     * @param key ключ для поиска
     * @param isCaseSensitive чувствительность к регистру
     * @param manual ручной ввод
     * @return Set<FormDataSearchResult> - Набор из номера столбца, строки, и самой найденной подстроки
     */
    PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive, boolean manual);

    /**
     * Копирование строк из сохраненного среза НФ-источника во временный срез НФ-приемника.
     * Временный срез приемника предварительно очищается.
     * Не копирует версию ручного ввода!
     *
     * @param formDataSourceId НФ источник
     * @param formDataDestinationId НФ приемник
     */
    void copyRows(long formDataSourceId, long formDataDestinationId);

    /**
     * Создает точку восстановления при ручном редактировании. Работает как с обычной, так и с версией ручного ввода.
     * @param formData
     */
    void createCheckPoint(FormData formData);

	/**
	 * Удаляет точку восстановления, сделанную перед редактированием данных (бывший commit)
	 *
	 * @param formData
	 */
	void removeCheckPoint(FormData formData);

	/**
	 * Откатывает все изменения и восстанавливает данные из контрольной точки
	 *
	 * @param formData
	 */
	void restoreCheckPoint(FormData formData);

}
