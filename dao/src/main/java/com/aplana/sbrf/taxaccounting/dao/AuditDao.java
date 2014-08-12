package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;
import java.util.List;

/**
 * DAO-Интерфейс для работы с журналом аудита
 */
public interface AuditDao {

    static final Integer[] AVAILABLE_CONTROL_EVENTS = {1,2,3,4,5,6,7,101,102,103,104,105,106,107,108,109,110,111,112,203,204,205,206,207,208,209,210, 301,302,303, 401,901,902,903};

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

    PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilter filter, List<Integer> departments, List<Integer> BADepartmentIds);

}
