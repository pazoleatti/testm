package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO-Интерфейс для работы с журналом аудита
 */
public interface AuditDao {

    //Номера выборок, списки которых передаем при выборе логов
    public enum SAMPLE_NUMBER {
        S_10,
        S_45,
        S_55
    }

	/**
	 * Получить информацию из журнала аудита по фильтру (сейчас только для Админа)
	 * @param logSystemFilter фильтр по которому происходит поиск необходимых данных
	 * @return объект, представляющий искомую информацию из журанала аудита
	 * */
	PagingResult<LogSearchResultItem> getLogsForAdmin(LogSystemFilter logSystemFilter);

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
     * Удалить информацию о логировании.
     * @param integers идентификаторы
     */
    void removeRecords(LogSystemFilter filter);

    /**
     * Дата последней архивации
     * @return дата
     */
    Date lastArchiveDate();

    /**
     * Дата последней архивации
     * @return дата
     */
    Date firstDateOfLog();

    /**
     * Возвращает записи ЖА для определенной роли{@link com.aplana.sbrf.taxaccounting.model.TARole},
     * подразделение которых является источником/приемником для соответствующей роли выборке
     * http://conf.aplana.com/pages/viewpage.action?pageId=14813541
     * @param availableDepIds параметры передающие список доступных подразделений
     * @return данные
     */
    PagingResult<LogSearchResultItem> getLogsBusinessForControl(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds);

    /**
     * Получает количество записей в таблице для админа
     * @param filter до какой даты
     * @return число записей в таблице до этой даты
     */
    long getCountForControl(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds);

    PagingResult<LogSearchResultItem> getLogsBusinessForOper(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds);

    long getCountForOper(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds);

    PagingResult<LogSearchResultItem> getLogsBusinessForControlUnp(LogSystemFilter filter);

    long getCountForControlUnp(LogSystemFilter filter);

    /**
     * Получает количество записей в таблице для админа
     * @param filter до какой даты
     * @return число записей в таблице до этой даты
     */
    long getCount(LogSystemFilter filter);
}
