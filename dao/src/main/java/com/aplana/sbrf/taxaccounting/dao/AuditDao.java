package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;
import java.util.List;

/**
 * DAO-Интерфейс для работы с журналом аудита
 */
public interface AuditDao {
	/**
	 * Получить информацию из журнала аудита по фильтру
	 * @param logSystemFilter фильтр по которому происходит поиск необходимых данных
	 * @return объект, представляющий искомую информацию из журанала аудита
	 * */
	PagingResult<LogSearchResultItem> getLogs(LogSystemFilter logSystemFilter);

	/**
	 * Добавить информацию об логировании
	 */
	void add(LogSystem logSystem);

    /**
     * Удалить информацию о логировании.
     * @param integers идентификаторы
     */
    void removeRecords(List<Long> integers);

    /**
     * Дата последней архивации
     * @return дата
     */
    Date lastArchiveDate();

    PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilter filter, List<Integer> departments);

}
