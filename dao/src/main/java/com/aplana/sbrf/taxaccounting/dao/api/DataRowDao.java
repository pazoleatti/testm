package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

import java.util.Collection;
import java.util.List;

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
	@Deprecated
	List<DataRow<Cell>> getSavedRows(FormData formData, DataRowRange range);
	
	/**
	 * Метод получает количество строк сохранненого среза
	 */
	int getSavedSize(FormData formData);

	/*
	 * Методы для работы с редактируемым срезом формы
	 */

	/**
	 * Метод получает строки редактируемого в данный момент среза строк НФ.
	 * 
	 */
	@Deprecated
	List<DataRow<Cell>> getRows(FormData formData, DataRowRange range);

	/**
	 * Метод получает количество строк редактируемого среза
	 */
	int getSize(FormData formData);

    /**
     * Метод получает количество строк редактируемого среза без учета итоговых
     */
    int getSizeWithoutTotal(FormData formData);

	/**
	 * Обновляет существующие строки НФ во временном срезе.
	 */
	void updateRows(FormData formData, Collection<DataRow<Cell>> rows);

	/**
	 * Удаляет строки во временном срезе.
	 *
	 * @param formData
	 * @param rows
	 */
	void removeRows(FormData formData, List<DataRow<Cell>> rows);

	/**
	 * Сохраняет все строки во временном срезе формы, при этом сохраняется порядок, и 
	 * удаляются все существующие строки. Фактически метод ведет себя как старый способ сохранения формы.
	 * Поля DataRow.index и DataRow.id не принимаются во внимание. 
	 * 
	 * @param formData
	 * @param rows
	 */
	void saveRows(FormData formData, List<DataRow<Cell>> rows);

	/**
	 * Вставляет строки начиная с указанного индекса
	 * @param formData куда вставляем
	 * @param index начальный индекс
	 * @param rows список новых строк
	 */
	void insertRows(FormData formData, int index, List<DataRow<Cell>> rows);

	/*
	 * Сохранение/отмена
	 */

	/**
	 * Делает временный срез строк формы постоянным.
	 * 
	 * @param formDataId
	 */
	void commit(FormData formData);

	/**
	 * Откатывает временный срез формы к постоянному.
	 * 
	 * @param formDataId
	 */
	void rollback(FormData formData);


    /**
     * Поиск по налоговой форме,
     * ищутся совпадения и выдается номер строки и столбца
     * на форме
     *
     * @param formDataId модель формы
     * @param formTemplateId идентификатор шаблона формы
     * @param range информация о выборке данных, с какой строки и сколько строк выбрать
     * @param key ключ для поиска
     * @param isCaseSensitive чувствительность к регистру
     * @return Set<FormDataSearchResult> - Набор из номера столбца, строки, и самой найденной подстроки
     */
    PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive);

    /**
     * Изменилось ли количество строк в табличной части до и после редактирования (до сохранения НФ)
     *
     * @param formId идентификатор экземпляра НФ
     * @return true - изменилось, false - не изменилось
     */
    boolean isDataRowsCountChanged(long formId);

    /**
     * Удаление значений неактуальных граф
     * @param columnIdList Список Id измененных/удаленных граф
     */
    void cleanValue(Collection<Integer> columnIdList);

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
     * Сохранить отсортированные строки без учета остальных изменении. Обновятся только значения атрибута DATA_ROW.ORD
     */
    void saveSortRows(List<DataRow<Cell>> dataRows, FormData formData);

//  ###################################### НОВАЯ СТРУКТУРА ХРАНЕНИЯ ######################################

	/**
	 * Создает временный срез, предварительно удалив из него старые данные. Работает как с обычной, так и с версией
	 * ручного ввода.
	 * @param formData
	 */
	void createTemporary(FormData formData);

	/**
	 * Удаляет строки из временного среза в диапазоне индексов.
	 * *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 * @param range диапазон удаляемых строк, индекс начинается с 1
	 */
	void removeRows(FormData formData, DataRowRange range);

	/**
	 * Удаляем все строки из временного
	 *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 */
	void removeRows(FormData formData);

	/**
	 * Актуализирует список ссылок НФ на элементы справочника. Ссылки выставляются только для строк постоянного среза
	 * (автоматическая или версия ручного ввода)
	 * @param formData экземпляр НФ, ссылки которого требуется актуализировать
	 */
	void refreshRefBookLinks(FormData formData);
}
