package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;

/**
 * Сервис для работы с журналом аудита
 */
public interface AuditService {
    public enum AsyncNames{
        LOG_FILTER,
        LOG_COUNT,
        LOG_FIRST_DATE,
        LOG_LAST_DATE
    }

	/**
	 * Получить информацию из журнала аудита по фильтру
	 * @param logSystemFilter фильтр по которому происходит поиск необходимых данных
	 * @return объект, представляющий искомую информацию из журанала аудита
	 * */
	PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter logSystemFilter);

	/**
	 * Добавить информацию об логировании.
     * Устаревший по причине того, что параметр formTypeId является излишним
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param departmentId подразделение НФ/декларации (необязательное)
     * @param reportPeriodId отчетный период (необязательное)
     * @param declarationTypeName наименование типа декларации (необязательное)
     * @param formTypeName наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param formKindId вид НФ (необязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId сыылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     * @param formTypeId идентификатор вид налоговой формы (в бд протсо как число, не ссылка на FORM_TYPE, заполнение согласно http://conf.aplana.com/pages/viewpage.action?pageId=9580637)
	 */
    @Deprecated
	void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
             String declarationTypeName, String formTypeName, Integer formKindId, String note, String blobDataId, Integer formTypeId);

    /**
     * Добавить информацию об логировании
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param departmentId подразделение НФ/декларации (необязательное)
     * @param reportPeriodId отчетный период (необязательное)
     * @param declarationTypeName наименование типа декларации (необязательное)
     * @param formTypeName наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param formKindId вид НФ (необязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId сыылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
             String declarationTypeName, String formTypeName, Integer formKindId, String note, String blobDataId);

    /**
     * Добавить информацию об логировании
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param departmentId подразделение НФ/декларации (необязательное)
     * @param reportPeriodName наименование отчетного периода
     * @param declarationTypeName наименование типа декларации (необязательное)
     * @param formTypeName наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param formKindId вид НФ (необязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId сыылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo,  String reportPeriodName, Integer departmentId,
             String declarationTypeName, String formTypeName, Integer formKindId, String note, String blobDataId);

    /**
     * Добавить информацию о логировании из версий макетов
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param startDate дата начала действия макета
     * @param endDate дата окончания действия макета
     * @param declarationTemplateName наименование типа декларации (необязательное)
     * @param formTemplateName наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param note пояснение (необязательное)
     * @param blobDataId сыылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo, Date startDate, Date endDate,
             String declarationTemplateName, String formTemplateName, String note, String blobDataId);

    /**
     * Логгирование для НФ/деклараций(т.к. нужно еще инфо о корр. периоде добавлять)
     * @param event
     * @param userInfo
     * @param declarationData
     * @param formData
     * @param note
     * @param blobDataId
     */
    void add(FormDataEvent event, TAUserInfo userInfo, DeclarationData declarationData, FormData formData, String note, String blobDataId);

    /**
     * Удаляем набор записей из журнала по фильтру
     * @param filter фильтр для удаления записей
     * @param firstRecord первая запись
     */
    void removeRecords(LogSystemFilter filter, LogSearchResultItem firstRecord, LogSearchResultItem lastRecord, TAUserInfo userInfo);

    /**
     * Получение даты последней архивации
     * @return дата ахивации
     */
    Date getLastArchiveDate();

    /**
     * Получить информацию об изменениях в НФ/декларациях из журнала аудита по фильтру
     * @param filter фильтр
     * @return записи из ЖА
     */
    PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilter filter, TAUserInfo userInfo);

    long getCountRecords(LogSystemFilter filter,  TAUserInfo userInfo);

    /**
     * Получение даты первой записи в журнале аудита
     * @return дата первой записи
     */
    Date getFirstDateOfLog();
}
