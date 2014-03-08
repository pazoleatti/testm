package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

import java.util.Date;
import java.util.List;

/**
 * DAO для работы с данными по налоговым формам
 */
public interface FormDataDao {
	/**
	 * Получить данные по налоговой формы
	 *
	 * @param formDataId идентификатор заполненной налоговой формы
	 * @return данные по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException,
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
	 * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException,
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
	 * Удалить запись о данных по налоговой форме
	 *
	 * @param formDataId идентификатор записи
	 */
	void delete(long formDataId);

	/**
	 * Ищет налоговую форму по заданным параметрам.
	 * Предполагается, что для большинства налоговых форм такое условие будет определять не более одной формы.
	 * Единственное возможное исключение - налог на имущество (для него использовать данный метод нельзя). 
	 * @param formTypeId   идентификатор {@link com.aplana.sbrf.taxaccounting.model.FormType вида формы}.
	 * @param kind         тип формы
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 * @param reportPeriodId идентификатор {@link com.aplana.sbrf.taxaccounting.model.ReportPeriod отчетного периода}
	 * @return форма или null, если такой формы не найдено
	 * @throws DaoException если будет найдено несколько записей, удовлетворяющих условию поиска
	 */
	FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);

    List<Long> findFormDataByFormTemplate(int formTemplateId);

	/**
	 * Поиск налоговой формы
	 * @param departmentId подразделение
	 * @param reportPeriodId отчетный период
	 * @return список налоговых форм, удовлетворяющих критерию
	 */
	List<FormData> find(int departmentId, int reportPeriodId);

    /**
     * Поиск ежемесячной налоговой формы
     * @param formTypeId Вид формы
     * @param kind Тип формы
     * @param departmentId Подразделение
     * @param taxPeriodId Налоговый период
     * @param periodOrder Порядковый номер (равен номеру месяца, при нумерации с 1)
     * @return Форма или null, если такой формы не найдено
     * @throws DaoException если будет найдено несколько записей, удовлетворяющих условию поиска
     */
    FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder);

	/**
	 * Обновление признака возврата
	 * 
	 * @param id
	 * @param returnSign
	 */
	void updateReturnSign(long id, boolean returnSign);
	
	/**
	 * Обновление статуса
	 * 
	 * @param id
	 * @param workflowState
	 */
	void updateState(long id, WorkflowState workflowState);

    void updatePeriodOrder(long id, int periodOrder);

    List<Long> getFormDataListInActualPeriodByTemplate(int templateId, Date startDate);
}
