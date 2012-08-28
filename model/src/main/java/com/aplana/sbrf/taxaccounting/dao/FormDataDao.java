package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;

/**
 * DAO для работы с данными по налоговым формам
 */
public interface FormDataDao {
	/**
	 * Получить данные по налоговой формы
	 * @param formDataId идентификатор заполненной налоговой формы
	 * @return данные по налоговой форме
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
}
