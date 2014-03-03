package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO для работы справочника оборотной ведомости
 * User: ekuvshinov
 */
public interface RefBookIncome101Dao {

	static final Long REF_BOOK_ID = 50L;

	static final String TABLE_NAME = "INCOME_101";

    /**
     * Получение записей справочника
     *
     * @param reportPeriodId
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Перегруженная функция с восходящей сортировкой по умолчанию
     *
     * @param reportPeriodId
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получение записи справочника по recordId
     * @param recordId
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Перечень версий записей за период
     * @return
     */
    List<Date> getVersions(Date startDate, Date endDate);

	/**
	 * Перечень версий записей за период
	 * @return
	 */
	List<Date> getVersions(String tableName, Date startDate, Date endDate);

    /**
     * Обновление записей справочника
     * @param records
     */
    void updateRecords(List<Map<String, RefBookValue>> records);

	/**
	 * Удалить записи
	 * @param uniqueRecordIds идентификаторы записей
	 */
	void deleteRecords(List<Long> uniqueRecordIds);
}
