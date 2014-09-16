package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

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
     * @param manual признак версии ручного ввода (возможно значение null)
     * @return данные по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException,
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
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException,
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
	 * Ищет налоговую форму по заданным параметрам (формы в корректирующем периоде не найдутся)
     * @deprecated Неактуально с появлением корректирующих периодов
	 */
    @Deprecated
	FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);

    List<Long> findFormDataByFormTemplate(int formTemplateId);

    /**
     * Поиск ежемесячной налоговой формы (формы в корректирующем периоде не найдутся)
     * @deprecated Неактуально с появлением корректирующих периодов
     */
    @Deprecated
    FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder);

    /**
     * Поиск НФ по отчетному периоду (и месяцу)
     * Если есть формы корректирующих периодов, то вернется форма корректирующего периода с максимальной датой (CORRECT_DATE)
     * @param formTypeId Вид НФ
     * @param kind Тип НФ
     * @param departmentReportPeriodId Отчетный период
     * @param periodOrder Порядковый номер (равен номеру месяца, при нумерации с 1) для ежемесячных форм
     */
    FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder);

    /**
     * Поиск НФ. Не учитывает корректирующий период, т.е. результатом могут быть как id экземпляров
     * корректирующих и/или некорректирующих периодов.
     * @param departmentIds подразделения
     * @param reportPeriodId отчетный период
     */
    List<FormData> find(List<Integer> departmentIds, int reportPeriodId);

	/**
	 * Обновление признака возврата
	 */
	void updateReturnSign(long id, boolean returnSign);
	
	/**
	 * Обновление статуса
	 */
	void updateState(long id, WorkflowState workflowState);

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
     * @param taxTypes типы налога
     * @param departmentIds подразделения
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
     * Обноляет имя подразделения(если у подразделения тип "ТБ")
     * @param departmentId идентификатор обновляемого подразделения
     * @param newDepartmentName новое имя подразделеия ТБ
     * @param dateFrom дата начала периода, с которой искать НФ
     * @param dateTo дата окончания периода, до которой искать НФ
     */
    void updateFDPerformerTBDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo);

    /**
     * Обноляет "вторую часть" имени подразделения. Вторая часть строки - если символ "/" в строке есть, то все символы после символа "/", иначе вся строка.
     * @param departmentId идентификатор обновляемого подразделения
     * @param newDepartmentName новое имя подразделения ТБ
     * @param dateFrom дата начала периода, с которой искать НФ
     * @param dateTo дата окончания периода, до которой искать НФ
     */
    void updateFDPerformerDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo);

    /**
     * Получить упорядоченный список экземпляров НФ предшествующих до указанного экземпляра, в пределах налогового периода
     *
     * @param formData экземпляр НФ
     * @param taxPeriod налоговый период
     * @return упорядоченный список экземпляров НФ
     */
    List<FormData> getPrevFormDataList(FormData formData, TaxPeriod taxPeriod);

    /**
     * Получить упорядоченный список экземпляров НФ, следующих после указанного экземпляра, в пределах налогового периода
     *
     * @param formData экземпляр НФ
     * @param taxPeriod налоговый период
     * @return упорядоченный список экземпляров НФ
     */
    List<FormData> getNextFormDataList(FormData formData, TaxPeriod taxPeriod);

    /**
     * TODO - возможно лучше сделать batchUpdate
     * Обновить значение атрибута "Номер последней строки предыдущей НФ"
     * @param formDataId идентификатор налоговой формы
     * @param previousRowNumber номер последней строки предыдущей НФ
     */
    void updatePreviousRowNumber(Long formDataId, Integer previousRowNumber);

    /**
     * Получить налоговые формы которые имеют признак ручного ввода
     * @param departments список подразделений
     * @param reportPeriodId отчетный период
     * @param taxType тип налога
     * @param kind тип налоговой формы
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список налоговых форм
     */
    List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind,
                                       Date periodStart, Date periodEnd);

    /**
     * Получить все существующие экземпляры НФ указанного макета НФ
     *
     * @param formTemplateId идентификатор макета НФ
     * @return список экземпляров НФ
     */
    List<FormData> getFormDataListByTemplateId(Integer formTemplateId);

    /**
     * НФ созданная в последнем отчетном периоде подразделения
     */
    FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder);
}
