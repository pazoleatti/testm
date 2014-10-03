package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с журналом аудита
 */
public interface AuditService {

	/**
	 * Получить информацию из журнала аудита по фильтру
	 * @param logSystemFilter фильтр по которому происходит поиск необходимых данных
	 * @return объект, представляющий искомую информацию из журанала аудита
	 * */
	PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter logSystemFilter);

	/**
	 * Добавить информацию об логировании
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param departmentId подразделение НФ/декларации (необязательное)
     * @param reportPeriodId отчетный период (необязательное)
     * @param declarationType наименование типа декларации (необязательное)
     * @param formType наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param formKindId вид НФ (необязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId сыылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     * @param formTypeId идентификатор вид налоговой формы (в бд протсо как число, не ссылка на FORM_TYPE, заполнение согласно http://conf.aplana.com/pages/viewpage.action?pageId=9580637)
	 */
	void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
             String declarationType, String formType, Integer formKindId, String note, String blobDataId, Integer formTypeId);

    /**
     * Удаляем набор записей из журнала и сразу создаем запись в ЖА об архивировании.
     * @param itemList архивированные записи
     */
    void removeRecords(List<LogSearchResultItem> itemList, TAUserInfo userInfo);

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

    /**
     * Блокировка операции "Архивирование журнала событий"
     * @param userInfo
     */
    LockData lock(TAUserInfo userInfo);

    /**
     * Снимает блокировку операции "Архивирование журнала событий"
     * @param userInfo
     */
    void unlock(TAUserInfo userInfo);
}
