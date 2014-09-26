package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с {@link FormData данными по налоговым формам}
 * @author dsultanbekov
 */
public interface FormDataService {
    /**
     * Создание НФ
     * @param logger Логгер
     * @param userInfo Пользователь-инициатор операции
     * @param formTemplateId Макет
     * @param departmentReportPeriodId Отчетный период подразделения
     * @param kind Тип НФ
     * @param periodOrder Номер месяца для ежемесячных форм (для остальных параметр отсутствует)
     * @return Id НФ
     */
    long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId,
                        FormDataKind kind, Integer periodOrder);

    /**
     * Создает версию ручного ввода
     * @param logger логгер-объект для фиксации диагностических сообщений
     * @param userInfo данные пользователя
     * @param formDataId идентификатор формы, для которой создается версия ручного ввода
     */
    void createManualFormData(Logger logger, TAUserInfo userInfo, Long formDataId);

    /**
     * Импорт ТФ НФ
     */
    void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream is,
                        String fileName, FormDataEvent formDataEvent);

    /**
     * Метод для импорта данных из xls-файлов
     * 
     * @param logger
     * @param userInfo
     * @param formDataId
     * @param is
     * @param fileName
     * 
     * TODO (sgoryachkin) заменить параметры is и fileName на uid блоба
     */
    void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream is, String fileName);

    /**
     * Метод для импорта данных из модуля миграции
     *
     * @param logger
     * @param userInfo
     * @param formDataId
     * @param is
     * @param fileName
     *
     */
    void migrationFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream is, String fileName);
	
	/**
	 * Выполнить расчёты по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCalc(Logger logger, TAUserInfo userInfo, FormData formData);

	/**
	 * Выполнить проверки по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCheck(Logger logger, TAUserInfo userInfo, FormData formData);

	/**
	 * Сохранить данные по налоговой форме
	 * Если форма новая то блокирует её после сохранения.
	 * 
	 * @param userInfo информация о пользователея, выполняющего операцию
	 * @param formData объект с данными налоговой формы
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав редактировать налоговую форму с такими параметрами
	 */
	long saveFormData(Logger logger, TAUserInfo userInfo, FormData formData);
	
	/**
	 * Получить данные по налоговой форме
	 *
     * @param userInfo информация о пользователе, выполняющего операцию
     * @param formDataId идентификатор записи, которую необходимо считать
     * @param manual нужна версия ручного ввода?
     * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав просматривать налоговую форму с такими параметрами
	 */
	FormData getFormData(TAUserInfo userInfo, long formDataId, boolean manual, Logger logger);

	/**
	 * Удалить данные по налоговой форме
	 * @param userInfo информация о пользователе, выполняющего операцию
	 * @param formDataId идентификатор записи, котрую нужно удалить
     * @param manual признак версии ручного ввода
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя недостаточно прав для удаления записи
	 */
	void deleteFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean manual);

	/**
	 * Выполнить изменение статуса карточки
	 * @param formDataId идентификатор объекта {@link FormData}
     * @param manual признак версии ручного ввода
	 * @param userInfo информация о пользователе, от имени которого выполняется действие
	 * @param move @{link WorkflowMove переход жизненного цикла}, который нужно выполнить
	 * @param note Причина возврата (перехода) по ЖЦ в Системе
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException
	 */
	void doMove(long formDataId, boolean manual, TAUserInfo userInfo, WorkflowMove move, String note, Logger logger);

    /**
     * Создаёт налоговую форму без проверок прав доступа
     * Метод предназначен для использования при реализации механизма консолидации, когда требуется создавать формы в чужих подразделениях
     * @param logger объект журнала
     * @param userInfo информация о пользователе, выполняющего операцию
     * @param formTemplateId идентификатор шаблона формы
     * @param departmentReportPeriodId Отчетный период подразделения
     * @param kind тип налоговой формы
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
     * @param importFormData признак импорта
     * @return созданный объект FormData (еще не сохранённый в БД)
     */
	long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId,
                                    FormDataKind kind, Integer periodOrder, boolean importFormData);

	/**
	 * Добавляет строку в форму и выполняет соответствующие скрипты.
	 *
	 * @param logger логгер для регистрации ошибок
	 * @param userInfo информация о пользователе
	 * @param formData данные формы
	 * @param currentDataRow текущая выбранная строка в НФ. Может использоваться для того, чтобы вставить новую строку после текущей
	 */
	void addRow(Logger logger, TAUserInfo userInfo, FormData formData, DataRow<Cell> currentDataRow);
	
	/**
	 * Удаляет строку из формы и выполняет соответствующие скрипты.
	 *
	 * @param logger логгер для регистрации ошибок
	 * @param userInfo информация о пользователе
	 * @param formData данные формы
	 * @param currentDataRow текущая выбранная строка в НФ, которую пытаемся удалить
	 */
	void deleteRow(Logger logger, TAUserInfo userInfo, FormData formData, DataRow<Cell> currentDataRow);

    /**
     * Поиск НФ в отчетном периоде подразделений
     */
    FormData findFormData(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder);

    /**
	 * Заблокировать FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userInfo информация о пользователе
	 * true - если удалось заблокировать налоговую форму, иначе - false
	 * */
	void lock(long formDataId, TAUserInfo userInfo);
	
	/**
	 * Снять блокировку с FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userInfo информация о пользователе
	 * true - если удалось разблокировать налоговую форму, иначе - false
	 * */
	void unlock(long formDataId, TAUserInfo userInfo);

	/**
	 * Получить информацию о состоянии блокировки налоговой формы.
	 * @param formDataId - идентификатор налоговой формы
	 * @return информацию о блокировке объекта
	 */
	LockData getObjectLock(long formDataId, TAUserInfo userInfo);

    /**
     * Ищет налоговые формы, которые использует данную версию макета, у которых период от #startDate
     * http://conf.aplana.com/pages/viewpage.action?pageId=11377482
     * @param templateId идентификатор версии
     * @param startDate дата начала актуализации
     * @return список идентификаторов НФ
     */
    List<Long> getFormDataListInActualPeriodByTemplate(int templateId, Date startDate);

    /**
     * Проверяет существование версии ручного ввода для указанной нф
     * @param formDataId идентификатор налоговой формы
     * @return версия ручного ввода существует?
     */
    boolean existManual(Long formDataId);

    /**
     * Поиск НФ. Не учитывает корректирующий период, т.е. результатом могут быть как id экземпляров
     * корректирующих и/или некорректирующих периодов.
     * @param departmentIds подразделения
     * @param reportPeriodId отчетный период
     */
    List<FormData> find(List<Integer> departmentIds, int reportPeriodId);

    /**
     * Проверяет наличие налоговых форм без привязки к периоду
     * @return
     * @param formTypeId
     * @param kind
     * @param departmentId
     * @param logger
     */
    boolean existFormData(int formTypeId, FormDataKind kind, int departmentId, Logger logger);

    boolean existFormDataByTaxAndDepartment(List<TaxType> taxTypes, List<Integer> departmentIds);

    /**
     * Обновляет имена тербанков в печатных формах(полях для печатных форм)
     * @param depTBId идентификатор редактируемого подразделения
     * @param depName новое наименование
     * @param dateFrom дата отчетного периода, начиная с которой надл=о поменять наименование отчетного периода
     * @param dateTo
     */
    void updateFDTBNames(int depTBId,  String depName, Date dateFrom, Date dateTo);

    /**
     * Обновляет имена тербанков в печатных формах(полях для печатных форм, вторая часть имени)
     * @param depTBId идентификатор редактируемого подразделения
     * @param depName новое наименование
     * @param dateFrom дата отчетного периода, начиная с которой надо поменять наименование отчетного периода
     * @param dateTo дата отчетного периода, до которой надо поменять наименование отчетного периода
     */
    void updateFDDepartmentNames(int depTBId, String depName, Date dateFrom, Date dateTo);

    /**
     * Получить значение "Номер последней строки предыдущей НФ"
     * @param formData {@link com.aplana.sbrf.taxaccounting.model.FormData экземпляр НФ}
     * @return
     */
    Integer getPreviousRowNumber(FormData formData);

    /**
     * Обновить Номер последней строки предыдущей НФ
     * @param formData экземпляр НФ, для которой необходимо обновить
     */
    void updatePreviousRowNumber(FormData formData);

    /**
     * Обновить Номер последней строки предыдущей НФ
     *
     * @param formData экземпляр НФ, для которой необходимо обновить
     * @param logger   логгер для регистрации ошибок
     */
    void updatePreviousRowNumber(FormData formData, Logger logger);

    /**
     * Обновить Номер последней строки предыдущей НФ
     *
     * @param formData     экземпляр НФ, для которой необходимо обновить
     * @param formTemplate макет НФ
     * @param logger       логгер для регистрации ошибок
     */
    void updatePreviousRowNumber(FormData formData, FormTemplate formTemplate, Logger logger);

    /**
     * Получить налоговые формы которые имеют признак ручного ввода
     * @param departments список подразделений
     * @param reportPeriodId отчетный период
     * @param taxType тип налога
     * @param kind тип налоговой формы
     * @return список налоговых форм
     */
    List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind);

    /**
     * Проверяет ссылки на справочники в нф, действуют ли они в периоде формы
     * @param formData нф
     */
    void checkReferenceValues(Logger logger, FormData formData);

    /**
     * Обновление "Номер последней строки предыдущей НФ" всех существующих экземпляров НФ для указанного шаблона
     *
     * @param formTemplate макет НФ
     */
    void batchUpdatePreviousNumberRow(FormTemplate formTemplate);

    /**
     * НФ созданная в последнем отчетном периоде подразделения
     */
    FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder);
}
