package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * DAO для работы с данными по налоговым формам
 */
@ScriptExposed
public interface FormDataDao {
	/**
	 * Получить данные по налоговой формы
	 *
	 * @param formDataId идентификатор заполненной налоговой формы
	 * @return данные по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException,
	 *          если данных с таким id нет (также может возникнуть из-за других ошибок в слое Dao)
	 */
	FormData get(long formDataId);

	/**
	 * Данная функция нужна для получения информации по налоговой форме.
	 * Она возвращает FormData, которая содержит ТОЛЬКО следующую информацию:
	 * -идентификатор налоговой формы
	 * -идентификатор департамента, к которому принадлежит налоговая форма
	 * -идентификатор отчетного периода, к которому принадлежит налоговая форма
	 * -тип налоговой формы
	 * -состояние налоговой формы
	 * @param formDataId идентификатор  налоговой формы
	 * @return FormData, которая содержит только информацию по налоговой форме и не содержит данных налоговой формы.
	 * @throws com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException,
	 *          если данных с таким id нет (также может возникнуть из-за других ошибок в слое Dao)
	 */
	FormData getWithoutRows(long formDataId);

	/**
	 * Сохранить данные по налоговой форме
	 * При сохранении новых объектов поле id в объекте formData должно быть равно null,
	 *
	 * @param formData данные для сохранения
	 * @return идентификатор сохранённой записи, в случае новой налоговой формы - сгененирован при сохранении,
	 *         в случае уже существующей - совпадает с полем id объекта formData
	 */
	long save(FormData formData);

	/**
	 * Возвращает список идентификаторов данных по налоговым формам, имеющих указанный тип
	 *
	 * @param typeId тип налоговой формы
	 * @return список идентификаторов данных по налоговым формам, удовлетворяющих запросу
	 */
	List<Long> listFormDataIdByType(int typeId);

	/**
	 * Удалить запись о данных по налоговой форме
	 *
	 * @param formDataId идентификатор записи
	 */
	void delete(long formDataId);

	/**
	 * Ищет налоговую форму по заданным параметрам.
	 *
	 * @param formTypeId   идентификатор {@link com.aplana.sbrf.taxaccounting.model.FormType вида формы}.
	 * @param kind         тип формы
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 * @param periodId     идентификатор {@link com.aplana.sbrf.taxaccounting.model.ReportPeriod отчетного периода}
	 * @return форма или null, если не найдена
	 */
	public FormData find(int formTypeId, FormDataKind kind, int departmentId, int periodId);
}
