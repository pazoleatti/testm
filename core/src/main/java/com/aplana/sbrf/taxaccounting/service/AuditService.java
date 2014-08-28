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
	 */
	void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
             String declarationType, String formType, Integer formKindId, String note, String blobDataId);

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
