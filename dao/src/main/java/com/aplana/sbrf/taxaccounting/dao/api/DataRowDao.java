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

	/**
	 * Удаление значений неактуальных граф. Метод используется настройщиком при смене типов граф
	 * @param columnIdList Список Id измененных/удаленных граф
	 */
	void cleanValue(Collection<Integer> columnIdList);

	/**
	 * Делает временный срез строк формы постоянным.
	 *
	 * @param formData
	 */
	void commit(FormData formData);

	/**
	 * Копирование строк из сохраненного среза НФ-источника во временный срез НФ-приемника.
	 * Временный срез приемника предварительно очищается.Не копирует версию ручного ввода! Макеты источника и приемника
	 * должны быть одинаковыми
	 *
	 * @param formDataSourceId НФ источник
	 * @param formDataDestinationId НФ приемник
	 */
	void copyRows(long formDataSourceId, long formDataDestinationId);

	/**
	 * Создает версию ручного ввода, предварительно удалив из нее старые данные. Новые данные извлекаются из
	 * автоматической версии из постоянного среза.
	 * @param formData
	 */
	public void createManual(FormData formData);

	/**
	 * Создает временный срез, предварительно удалив из него старые данные. Работает как с обычной, так и с версией
	 * ручного ввода.
	 * @param formData
	 */
	void createTemporary(FormData formData);

	/**
	 * Метод получает строки редактируемого в данный момент среза строк НФ.
	 *
	 */
	List<DataRow<Cell>> getTempRows(FormData formData, DataRowRange range);

	/**
	 * Метод получает строки сохраненного среза строк НФ.
	 *
	 */
	List<DataRow<Cell>> getSavedRows(FormData formData, DataRowRange range);

	/**
	 * Метод получает количество строк сохранненого среза
	 */
	int getSavedSize(FormData formData);

	/**
	 * Метод получает количество строк редактируемого среза
	 */
	int getTempSize(FormData formData);

    /**
     * Метод получает количество строк редактируемого среза без учета итоговых
     */
    int getSizeWithoutTotal(FormData formData, boolean isTemp);

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
	 * Удаляем все строки из временного среза
	 *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 */
	void removeRows(FormData formData);

	/**
	 * Удаляем все строки ручной версии как из постоянного, так и из временного срезов
	 *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 */
	void removeAllManualRows(FormData formData);

	/**
	 * Удаляет строки из временного среза в диапазоне индексов.
	 * *
	 * @param formData экземпляр НФ для которой выполняется удаление строк
	 * @param range диапазон удаляемых строк, индекс начинается с 1
	 */
	void removeRows(FormData formData, DataRowRange range);

	/**
	 * Удаляет строки во временном срезе.
	 *
	 * @param formData
	 * @param rows
	 */
	void removeRows(FormData formData, List<DataRow<Cell>> rows);

	/**
	 * Сохранить отсортированные строки без учета остальных изменений. Обновятся только значения атрибута ORD.
	 * Порядок задается последовательностью элементов в rows
	 */
	void reorderRows(FormData formData, List<DataRow<Cell>> rows);

	/**
	 * Откатывает временный срез формы к постоянному. Удаляет все данные из временных срезов.
	 *
	 * @param formData
	 */
	void rollback(FormData formData);

	/**
	 * Сохраняет все строки во временном срезе формы, при этом удаляются все существующие строки.
	 * Поля DataRow.index и DataRow.id не принимаются во внимание. Порядок следования выставляется согласно
	 * последовательности строк в rows. Id выставляется новый из последовательности "seq_form_data_nnn"
	 * 
	 * @param formData
	 * @param rows
	 */
	void saveRows(FormData formData, List<DataRow<Cell>> rows);

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
	 * Обновляет существующие строки НФ во временном срезе.
	 */
	void updateRows(FormData formData, Collection<DataRow<Cell>> rows);

    /**
     * Сравнивает строки во временном срезе и основном
     * @param formData
     * @return true - изменении нету, иначе false
     */
    boolean compareRows(FormData formData);
}
