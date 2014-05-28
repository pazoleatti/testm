package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * DAO для работы с данными по налоговым формам
 */
public interface FormDataDao {
    /**
     * Получить данные по налоговой формы
     *
     *
     * @param formDataId идентификатор заполненной налоговой формы
     * @return данные по налоговой форме
     * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException,
     *          если данных с таким id нет (также может возникнуть из-за других ошибок в слое Dao)
     */
    @Deprecated
    FormData get(long formDataId);

	/**
	 * Получить данные по налоговой формы
	 *
	 *
     * @param formDataId идентификатор заполненной налоговой формы
     * @param manual признак версии ручного ввода
     * @return данные по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException,
	 *          если данных с таким id нет (также может возникнуть из-за других ошибок в слое Dao)
	 */
	FormData get(long formDataId, Boolean manual);

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

    List<Long> findFormDataByDepartment(int departmentId);

	/**
	 * Поиск налоговой формы
	 * @param departmentIds подразделения
	 * @param reportPeriodId отчетный период
	 * @return список налоговых форм, удовлетворяющих критерию
	 */
	List<FormData> find(List<Integer> departmentIds, int reportPeriodId);

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

    /**
     * Проверяет существование версии ручного ввода для указанной нф
     * @param formDataId идентификатор налоговой формы
     * @return версия ручного ввода существует?
     */
    boolean existManual(Long formDataId);

    /**
     * Получить список id форм типа/вида/подразделения без привязки к периоду
     * @param formTypeId тип формы
     * @param kind вид формы
     * @param departmentId подразделение
     * @return список id форм
     */
    List<Long> getFormDataIds(int formTypeId, FormDataKind kind, int departmentId);

    /**
     * Получить список id форм типа/вида/подразделения без привязки к периоду
     * @param formTypeId тип формы
     * @param kind вид формы
     * @param departmentId подразделение
     * @return список id форм
     */
    List<Long> getFormDataIds(List<TaxType> taxTypes, List<Integer> departmentIds);

    /**
     * Удаляет версию ручного ввода
     * @param formDataId идентификатор нф
     */
    void deleteManual(long formDataId);

    /**
     * Получить список строк
     * @param columnId идентификатор столбца
     * @param formTemplateTypeId идентификатор макета НФ
     * @return
     */
    List<String> getStringList(Integer columnId, Integer formTemplateTypeId);

    /**
     * Получить упорядоченный список экземпляров НФ предшествующих до указанного экземпляра, в пределах которых
     * устанавливается сквозная нумерация строк.
     *
     * @param year Календарный год налогового периода
     * @param departmentId идентификатор подразделения
     * @param code тип налоговой формы
     * @param kind вид налоговой формы
     * @param formDataId идентификатор налоговой формы
     * @return
     */
    List<FormData> getPrevFormDataListForCrossNumeration(Integer year, Integer departmentId, String code, Integer kind, Long formDataId);

    /**
     * Получить упорядоченный список экземпляров НФ, следующих после указанного экземпляра, в пределах которых
     * устанавливается сквозная нумерация строк.
     *
     * @param year Календарный год налогового периода
     * @param departmentId идентификатор подразделения
     * @param code тип налоговой формы
     * @param kind вид налоговой формы
     * @param formDataId идентификатор налоговой формы
     * @return
     */
    List<FormData> getNextFormDataListForCrossNumeration(Integer year, Integer departmentId, String code, Integer kind, Long formDataId);

    /**
     * TODO - возможно лучше сделать batchUpdate
     * Обновить значение атрибута "Номер последней строки предыдущей НФ"
     * @param formDataId идентификатор налоговой формы
     * @param previousRowNumber номер последней строки предыдущей НФ
     */
    void updatePreviousRowNumber(Long formDataId, Integer previousRowNumber);
}
