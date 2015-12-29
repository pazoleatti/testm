package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

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
     * @param comparativePeriodId период сравнения
     * @param accruing признак нарастающего итога
     * @param kind Тип НФ
     * @param periodOrder Номер месяца для ежемесячных форм (для остальных параметр отсутствует)
     * @param importFormData флаг создания при загрузке
     * @return Id НФ
     */
    long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId,
                        Integer comparativePeriodId, boolean accruing,
                        FormDataKind kind, Integer periodOrder, boolean importFormData);

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
     * Метод для импорта данных из xls-файлов(асинхронная задача)
     * 
     * @param logger
     * @param userInfo
     * @param formDataId
     * @param is
     * @param fileName
     * 
     * TODO (sgoryachkin) заменить параметры is и fileName на uid блоба
     */
    void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream is, String fileName, LockStateLogger stateLogger);

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
     * Выполнить обновление по налоговой форме
     * @param logger логгер-объект для фиксации диагностических сообщений
     * @param userInfo информация о пользователе, запросившего операцию
     * @param formData объект с данными по налоговой форме
     */
    void doRefresh(Logger logger, TAUserInfo userInfo, FormData formData);

	/**
	 * Выполнить проверки по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formData объект с данными по налоговой форме
     * @param editMode признак того, что операция вызвана из режима редактирования, когда временный срез уже создан
	 */
	void doCheck(Logger logger, TAUserInfo userInfo, FormData formData, boolean editMode);

	/**
	 * Сохранить данные по налоговой форме
	 * Если форма новая то блокирует её после сохранения.
	 * 
	 * @param userInfo информация о пользователея, выполняющего операцию
	 * @param formData объект с данными налоговой формы
     * @param editMode признак того, что операция вызвана для задачи из режима редактирования
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав редактировать налоговую форму с такими параметрами
	 */
	long saveFormData(Logger logger, TAUserInfo userInfo, FormData formData, boolean editMode);

    /**
     * Сохранить данные исполнителей и подписантов для налоговой формы
     * @param logger
     * @param userInfo
     * @param formData
     */
    void savePerformer(Logger logger, TAUserInfo userInfo, FormData formData);

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
	void doMove(long formDataId, boolean manual, TAUserInfo userInfo, WorkflowMove move, String note, Logger logger, boolean isAsync, LockStateLogger stateLogger);

    /**
     * Создаёт налоговую форму без проверок прав доступа
     * Метод предназначен для использования при реализации механизма консолидации, когда требуется создавать формы в чужих подразделениях
     * @param logger объект журнала
     * @param userInfo информация о пользователе, выполняющего операцию
     * @param formTemplateId идентификатор шаблона формы
     * @param departmentReportPeriodId Отчетный период подразделения
     * @param comparativePeriodId период сравнения
     * @param accruing признак нарастающего итога
     * @param kind тип налоговой формы
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
     * @param importFormData признак импорта
     * @return созданный объект FormData (еще не сохранённый в БД)
     */
	long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId,
                                    Integer comparativePeriodId, boolean accruing,
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
    FormData findFormData(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing);

    /**
	 * Заблокировать FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userInfo информация о пользователе
	 * true - если удалось заблокировать налоговую форму, иначе - false
	 * */
	void lock(long formDataId, boolean manual, TAUserInfo userInfo);
	
	/**
	 * Снять блокировку с FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userInfo информация о пользователе
	 * true - если удалось разблокировать налоговую форму, иначе - false
	 * */
	Boolean unlock(long formDataId, TAUserInfo userInfo);

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
     * Поиск НФ необходимых для формирования отчетности для МСФО
     * @param reportPeriodId
     * @return
     */
    List<FormData> getIfrsForm(int reportPeriodId);

    /**
     * Проверяет наличие налоговых форм без привязки к периоду
     * @return
     * @param formTypeId
     * @param kind
     * @param departmentId
     * @param logger
     */
    boolean existFormData(int formTypeId, FormDataKind kind, int departmentId, Logger logger);

    /**
     * Обновляет имена тербанков в печатных формах(полях для печатных форм)
     * Так же удаляются все отчеты с ними связанные
     * @param depTBId идентификатор редактируемого подразделения
     * @param depName новое наименование
     * @param dateFrom дата отчетного периода, начиная с которой надл=о поменять наименование отчетного периода
     * @param dateTo
     * @param isChangeTB true - показывает, что изменился тип подразделения с типа ТБ
     */
    void updateFDTBNames(int depTBId,  String depName, Date dateFrom, Date dateTo, boolean isChangeTB, TAUserInfo user);

    /**
     * Обновляет имена подразделений в печатных формах(полях для печатных форм, вторая часть имени)
     * Так же удаляются все отчеты с ними связанные
     * @param depTBId идентификатор редактируемого подразделения
     * @param depName новое наименование
     * @param dateFrom дата отчетного периода, начиная с которой надо поменять наименование отчетного периода
     * @param dateTo дата отчетного периода, до которой надо поменять наименование отчетного периода
     */
    void updateFDDepartmentNames(int depTBId, String depName, Date dateFrom, Date dateTo, TAUserInfo user);

    /**
     * Получить значение "Номер последней строки предыдущей НФ"
     * @param formData {@link com.aplana.sbrf.taxaccounting.model.FormData экземпляр НФ}
     * @param savingFormData сохраняемая НФ, для которой надо брать временный срез
     * @return
     */
    Integer getPreviousRowNumber(FormData formData, FormData savingFormData);

    /**
     * Обновить Номер последней строки предыдущей НФ
     * @param formData экземпляр НФ, для которой необходимо обновить
     */
    void updatePreviousRowNumber(FormData formData, TAUserInfo user);

    /**
     * Обновить Номер последней строки предыдущей НФ
     *  @param formData экземпляр НФ, для которой необходимо обновить
     * @param logger   логгер для регистрации ошибок
     * @param save
     * @param useZero признак того, что при пересчете начать с 0
     */
    void updatePreviousRowNumber(FormData formData, Logger logger, TAUserInfo user, boolean isSave, boolean useZero);

    /**
     * Обновить Номер последней строки предыдущей НФ
     *  @param formData     экземпляр НФ, для которой необходимо обновить
     * @param formTemplate макет НФ
     * @param logger       логгер для регистрации ошибок
     * @param save
     * @param useZero признак того, что при пересчете начать с 0
     */
    void updatePreviousRowNumber(FormData formData, FormTemplate formTemplate, Logger logger, TAUserInfo user, boolean isSave, boolean useZero);

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
     * @param needCheckTemp нужно проверить также временный срез
     */
    void checkReferenceValues(Logger logger, FormData formData, boolean needCheckTemp);

    /**
     * Обновление "Номер последней строки предыдущей НФ" всех существующих экземпляров НФ для указанного шаблона
     *
     * @param formTemplate макет НФ
     */
    void batchUpdatePreviousNumberRow(FormTemplate formTemplate, TAUserInfo user);

    /**
     * НФ созданная в последнем отчетном периоде подразделения
     */
    FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing);

    /**
     * НФ созданная в последнем отчетном периоде подразделения
     */
    List<FormData> getLastList(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder);

    /**
     * Принятый экземпляр НФ в предыдущем отчетном периоде
     * @param formData Текущий экземпляр НФ
     * @param departmentReportPeriodList Список всех отчетных периодов для пары подразделение-отчетный период. Должен
     *                                   быть отсортирован в том порядке, в котором требуется искать пред. период
     * @param departmentReportPeriod Текущий отчетный период подразделения
     */
    FormData getPreviousFormDataCorrection(FormData formData, List<DepartmentReportPeriod> departmentReportPeriodList,
                                          DepartmentReportPeriod departmentReportPeriod);

    /**
     * Получения списка всех возможных ключей для данного вида отчета
     * @param reportType
     * @param formDataId
     * @param manual
     * @return
     */
    List<String> generateReportKeys(ReportType reportType, long formDataId, Boolean manual);

    /**
     * Генерация ключа блокировки для задачи формирования отчета
     * @param formDataId
     * @param reportType
     * @param showChecked
     * @param manual
     * @param saved
     * @return
     */
    String generateReportKey(long formDataId, FormDataReportType reportType, boolean showChecked, boolean manual, boolean saved);
    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с НФ
     * @param formDataId идентификатор налоговой формы
     * @param manual признак версии ручного ввода. Если null - то удаляются отчеты для обеих версий
     */
    void deleteReport(long formDataId, Boolean manual, int userId, String cause);

    void findFormDataIdsByRangeInReportPeriod(int formTemplateId, Date startDate, Date endDate, Logger logger);

    /**
     * Проверки перед консолидацией
     * @param formData
     * @param userInfo
     * @param logger
     */
    void checkCompose(final FormData formData, TAUserInfo userInfo, Logger logger);

    /**
     * Выводит список не принятых и не созданных источников при консолидации
     * @param formDataId
     * @param manual
     * @param userInfo
     * @param logger
     */
    void checkSources(long formDataId, boolean manual, TAUserInfo userInfo, Logger logger);

    /**
     * Консолидация НФ
     * @param workflowMove
     * @param formData
     * @param userInfo
     * @param logger
     */
    void compose(final FormData formData, TAUserInfo userInfo, Logger logger, LockStateLogger stateLogger);

    /**
     * Получает полное название нф с указанием подразделения, периода и прочего
     * @param formDataId идентификатор нф
     * @param str название файла(при импорте ТФ)/название кнопки(при переходе по ЖЦ) Может быть null
     * @param reportType тип отчета. Может быть null
     * @return название
     */
    String getFormDataFullName(long formDataId, boolean manual, String str, ReportType reportType, String... args);

    /**
     * Генерация ключа блокировки для задачи c типом reportType
     * @param formDataId
     * @param reportType тип задачи
     * @return
     */
    String generateTaskKey(long formDataId, ReportType reportType);

    /**
     * Получение блокировки и её типа для НФ, null если для НФ нет блокировок
     * @param formDataId
     * @return
     */
    Pair<ReportType, LockData> getLockTaskType(long formDataId);

    /**
     * Удаление блокировок для задач(отмена задачи)
     * @param formDataId
     * @param userInfo
     * @param reportTypes
     */
    void interruptTask(long formDataId, TAUserInfo userInfo, List<ReportType> reportTypes, String cause);

    /**
     * Вывод сообщения, что форма заблокирована
     * @param lockType
     * @param logger
     * @param reportType тип текущей операции
     */
    void locked(long formDataId, ReportType reportType, Pair<ReportType, LockData> lockType, Logger logger, String... args);

    /**
     * Проверка возможности изменения НФ
     * @param formDataId
     * @param logger
     * @param userInfo
     * @param taskName
     * @param editMode
     */
    void checkLockedByTask(long formDataId, Logger logger, TAUserInfo userInfo, String taskName, boolean editMode);

    /**
     *
     * @param userInfo
     * @param formData
     * @param reportType
     * @return
     */
    Long getValueForCheckLimit(TAUserInfo userInfo, FormData formData, ReportType reportType, String uuid, Logger logger);

    /**
     * Формирует название операции
     * @param reportType
     * @param formDataId
     * @param userInfo
     * @return
     */
    String getTaskName(ReportType reportType, long formDataId, TAUserInfo userInfo, String... args);

    /**
     * Проверяет существование операции, по которым требуется удалить блокировку
     * @param formDataId
     * @param reportType
     * @param logger
     * @param userInfo
     * @return
     */
    boolean checkExistTask(long formDataId, boolean manual, ReportType reportType, Logger logger, TAUserInfo userInfo);

    /**
     * Отмена операции, по которым требуется удалить блокировку(+удаление отчетов)
     * @param formDataId
     * @param reportType
     * @param userId
     */
    void interruptTask(long formDataId, Boolean manual, int userId, ReportType reportType, String cause);

    /**
     * Откатывает все изменения, отменяет асинх задачи, снимает блокирови и восстанавливает данные из контрольной точки
     *
     * @param formData
     */
    void restoreCheckPoint(long formDataId, boolean manual, TAUserInfo userInfo);

    /**
     * Проверка наличия изменении в НФ
     * @param formDataId
     * @return
     */
    boolean isEdited(long formDataId);

    /**
     * Получение заголовка таблицы
     * @param formData
     * @return
     */
    List<DataRow<HeaderCell>> getHeaders(FormData formData, TAUserInfo userInfo, Logger logger);

    /**
     * Получение данных по файлам для формы "Файлы и комментарии"
     */
    List<FormDataFile> getFiles(long formDataId);

    /**
     * Получения комментария для формы "Файлы и комментарии"
     */
    String getNote(long formDataId);

    /**
     * Сохранение данных формы "Файлы и комментарии"
     */
    void saveFilesComments(long formDataId, String note, List<FormDataFile> files);

    /**
     * Формирование специфического отчета НФ(отчет формируется в скриптами)
     * @param formData
     * @param isShowChecked
     * @param saved
     * @param specificReportType
     * @param userInfo
     * @param stateLogger
     */
    void createSpecificReport(FormData formData, boolean isShowChecked, boolean saved, String specificReportType, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Получить список доступных отчётов
     * @param formData
     * @param userInfo
     * @param logger
     * @return
     */
    List<String> getSpecificReportTypes(FormData formData, TAUserInfo userInfo, Logger logger);
}
