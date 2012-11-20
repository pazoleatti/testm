package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FilterData;
import com.aplana.sbrf.taxaccounting.model.FormData;

import java.util.List;

/**
 * DAO для работы с данными по налоговым формам
 */
public interface FormDataDao {
	/**
	 * Получить данные по налоговой формы
	 * @param formDataId идентификатор заполненной налоговой формы
	 * @return данные по налоговой форме
	 * @throws DaoException, если данных с таким id нет (также может возникнуть из-за других ошибок в слое Dao)
	 */
	FormData get(long formDataId);
	/**
	 * Сохранить данные по налоговой форме
	 * При сохранении новых объектов поле id в объекте formData должно быть равно null,
	 * @param formData данные для сохранения
	 * @return идентификатор сохранённой записи, в случае новой налоговой формы - сгененирован при сохранении,
	 * в случае уже существующей - совпадает с полем id объекта formData
	 */
	long save(FormData formData);

	/**
	 * Возвращает информацию по всем, имеющимся в наличии заполенным формам
	 * @return
	 */
	List<FormData> getAll();

    /**
     * Возвращает список идентификаторов данных по налоговым формам, имеющих указанный тип
     * @param filterData - фильтр, по которому происходит поиск
     * @return возвращает информацию по всем имеющимся в наличии заполенным формам, удовлетворяющие
     * запросу
     */
    List<FormData> findByFilter(FilterData filterData);

	/**
	 * Возвращает список идентификаторов данных по налоговым формам, имеющих указанный тип 
	 * @param typeId тип налоговой формы
	 * @return список идентификаторов данных по налоговым формам, удовлетворяющих запросу
	 */
	List<Long> listFormDataIdByType(int typeId);
	
	/**
	 * Удалить запись о данных по налоговой форме
	 * @param formDataId идентификатор записи
	 */
	void delete(long formDataId);
}
