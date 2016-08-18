package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
     * Сохранить данные исполнителей и подписантов для налоговой формы
     * @param formData
     */
    void savePerformerSigner(FormData formData);

	/**
	 * Удалить запись о данных по налоговой форме
	 *
     * @param ftId идентификатор версии макета
     * @param fdId идентификатор НФ
     */
	void delete(int ftId, long fdId);

    /**
     * Список Id экземпляров НФ по Id шаблона
     */
    List<Long> findFormDataByFormTemplate(int formTemplateId);

    /**
     * Поиск ежемесячной налоговой формы (формы в корректирующем периоде не найдутся)
     * @deprecated Неактуально с появлением корректирующих периодов
     */
    @Deprecated
    FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder);

    /**
     * Поиск НФ по отчетному периоду подразделения (и месяцу)
     * @param formTypeId Вид НФ
     * @param kind Тип НФ
     * @param departmentReportPeriodId Отчетный период подразделения
     * @param periodOrder Порядковый номер (равен номеру месяца, при нумерации с 1) для ежемесячных форм
     * @param comparativePeriodId Период сравнения - ссылка на DepartmentReportPeriod. Может быть null
     * @param accruing Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения)
     */
    FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing);

    /**
     * Поиск НФ. Не учитывает корректирующий период, т.е. результатом могут быть как id экземпляров
     * корректирующих и/или некорректирующих периодов.
     * @param departmentIds подразделения
     * @param reportPeriodId отчетный период
     */
    List<FormData> find(List<Integer> departmentIds, int reportPeriodId);

    /**
     * Поиск НФ необходимых для формирования отчетности для МСФО
     * @param reportPeriodId
     * @return
     */
    List<FormData> getIfrsForm(int reportPeriodId);

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
     * @param formData нф
     */
    void deleteManual(FormData formData);

    /**
     * Обноляет имя подразделения(если у подразделения тип "ТБ")
     * @param departmentId идентификатор обновляемого подразделения
     * @param newDepartmentName новое имя подразделеия ТБ
     * @param dateFrom дата начала периода, с которой искать НФ
     * @param dateTo дата окончания периода, до которой искать НФ
     * @param isChangeTB смена типа ТБ, влечет за собой изменения в печатных формах. Меняются первая часть имени
     *                   печ.форм, во всех НФ в которых есть этот ТБ и в формах принадлежащих этому ТБ.
     *                   true - меняется на "", а в НФ этого ТБ на новое имя, false-меняется на новое имя
     */
    void updateFDPerformerTBDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo, boolean isChangeTB);

    /**
     * Обноляет "вторую часть" имени подразделения. Вторая часть строки - если символ "/" в строке есть, то все символы
     * после символа "/", иначе вся строка.
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
     * Обновить значение атрибута "Номер последней строки предыдущей НФ"
     * @param formData налоговая форма
     * @param previousRowNumber номер последней строки предыдущей НФ
     */
    void updatePreviousRowNumber(FormData formData, Integer previousRowNumber);

	/**
	 * Обновить значение атрибута "Количество пронумерованных строк текущей НФ"
	 * @param formData налоговая форма
	 */
	void updateCurrentRowNumber(FormData formData);

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
     * НФ созданная в последнем отчетном периоде подразделения, если в нем нет формы, то берется форма из предыдущего
     * отчетного периода подразделения и т.д. в рамках отчетного периода
     * @param comparativePeriodId Период сравнения - ссылка на DepartmentReportPeriod. Может быть null
     * @param accruing Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения)
     */
    FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing);

    /**
     * НФ созданная в последнем отчетном периоде подразделения с отсутствующей датой корректировки или меньшей или
     * равной заданной, если в нем нет формы, то берется форма из предыдущего отчетного периода подразделения
     * и т.д. в рамках отчетного периода
     * @param comparativePeriodId Период сравнения - ссылка на DepartmentReportPeriod. Может быть null
     * @param accruing Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения)
     */
    FormData getLastByDate(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Date correctionDate, Integer comparativePeriodId, boolean accruing);

    /**
     * НФ созданные в последнем отчетном периоде подразделения с отсутствующей датой корректировки или меньшей или
     * равной заданной, если в нем нет формы, то берется форма из предыдущего отчетного периода подразделения
     * и т.д. в рамках отчетного периода.
     * Использование этого метода требуется только для обработки ежемесячных форм
     */
    List<FormData> getLastListByDate(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Date correctionDate);

    /**
     * Находим НФ, относящиеся к отчетным периодам, с которыми новый период актуальности версии макета не пересекается
     * @param formTemplateId идентификатор версии макета НФ
     * @param startDate дата, начиная с которой искать пересечения
     * @param endDate дата, до которой искать
     * @return идентификаторы
     */
    List<Integer> findFormDataIdsByRangeInReportPeriod(int formTemplateId, Date startDate, Date endDate);

    /**
     * Обновить значение ручного ввода.
     *
     * @param formData форма нф
     */
    void updateManual(FormData formData);

    /**
     * Обновить значение признака сортировки
     * @param formDataId
     * @param isSorted
     */
    void updateSorted(long formDataId, boolean isSorted);

    /**
     * Сделать резервную копию признака сортировки
     * @param formDataId
     */
    void backupSorted(long formDataId);

    /**
     * Восстановить признак сотрировки из резервной копии
     * @param formDataId
     */
    void restoreSorted(long formDataId);

    void updateEdited(long formDataId, boolean isModification);

    boolean isEdited(long formDataId);

    /**
     * Обновления комментария НФ
     * @param formDataId
     * @param note
     */
    void updateNote(long formDataId, String note);

    /**
     * Получение комментария НФ
     * @param formDataId
     * @return
     */
    String getNote(long formDataId);

    /**
     * Проверяет существование НФ
     * @param formDataId
     * @return
     */
    boolean existFormData(long formDataId);
}
