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
	 * Создать налоговую форму заданного типа
	 * При создании формы выполняются следующие действия:
	 * 1) создаётся пустой объект
	 * 2) если в объявлении формы заданы строки по-умолчанию (начальные данные), то эти строки копируются в созданную форму
	 * 3) если в объявлении формы задан скрипт создания, то этот скрипт выполняется над создаваемой формой
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formTemplateId идентификатор шаблона формы, по которой создавать объект
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}, к которому относится форма
	 * @param kind {@link FormDataKind тип налоговой формы} (первичная, сводная, и т.д.), это поле необходимо, так как некоторые виды
	 *		налоговых форм в одном и том же подразделении могут существовать в нескольких вариантах (например один и тот же РНУ  на уровне ТБ
	 *		- в виде первичной и консолидированной)
	 * @param reportPeriod отчетный период в котором создается форма
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
	 * @return созданный и проинициализированный объект данных.
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав создавать налоговую форму с такими параметрами
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если при создании формы произошли ошибки, вызванные несоблюдением каких-то бизнес-требований, например отсутствием
	 *		обязательных параметров
	 */
	long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentId, FormDataKind kind,
                        ReportPeriod reportPeriod, Integer periodOrder);

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
    void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream is, String fileName, FormDataEvent formDataEvent);

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
    void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream is, String fileName);

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
	FormData getFormData(TAUserInfo userInfo, long formDataId, Boolean manual, Logger logger);

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
     * @param departmentId идентификатор подразделения
     * @param kind тип налоговой формы
     * @param reportPeriodId идентифиуатор отчетного периода
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
     * @param importFormData признак импорта
     * @return созданный объект FormData (еще не сохранённый в БД)
     */
	long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentId,
                                    FormDataKind kind, int reportPeriodId, Integer periodOrder, boolean importFormData);

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
     * Поиск формы по основным параметрам
     *
     * @param formTypeId
     * @param kind
     * @param departmentId
     * @param reportPeriodId
     * @param periodOrder
     * @return
     */
    FormData findFormData(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder);

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
	 * Снять все блокировки с FormData для пользователя
	 * @param userInfo информация о пользователе
	 * @return true - если удалось разблокировать, иначе - false
	 */
	@Deprecated
	boolean unlockAllByUser(TAUserInfo userInfo);

	/**
	 * Получить информацию о состоянии блокировки налоговой формы.
	 * @param formDataId - идентификатор налоговой формы
	 * @return информацию о блокировке объекта
	 */
	ObjectLock<Long> getObjectLock(long formDataId, TAUserInfo userInfo);

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
     * Проверяет, является ли указанная нф сводной формой банка - последней формой перед декларацией
     * @param formDataId идентификатор нф
     * @return форма - сводная банка?
     */
    boolean isBankSummaryForm(long formDataId);

    /**
     * Поиск налоговой формы
     * @param departmentIds подразделения
     * @param reportPeriodId отчетный период
     * @return список налоговых форм, удовлетворяющих критерию
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
     * Обновляет имена террбанков в печатных формах(полях для печатных форм, первая часть)
     * @param newDepTBId новый террбанк
     * @param oldDepTBId террбанк, который был
     * @param dateFrom дата отчетного периода, начиная с которой надл=о поменять наименование отчетного периода
     * @param dateTo
     */
    void updateFDTBNames(int newDepTBId, int oldDepTBId, Date dateFrom, Date dateTo);

    /**
     * Обновляет имена террбанков в печатных формах(полях для печатных форм, вторая часть имени)
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
     * @return
     */
    String updatePreviousRowNumber(FormData formData);

    /**
     * Получить налоговые формы которые имеют признак ручного ввода
     * @param departments список подразделений
     * @param reportPeriodId отчетный период
     * @param taxType тип налога
     * @param kind тип налоговой формы
     * @return список налоговых форм
     */
    List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind);
}
