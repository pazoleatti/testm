package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO для работы справочника "Отчет о прибылях и убытках (Форма 0409102-СБ)"
 * @author Dmitriy Levykin
 */
public interface RefBookIncome102Dao {

	public static final Long REF_BOOK_ID = 52L;
    static final Long INCOME_102_AP_REF_BOOK_ID = 107L;

	static final String TABLE_NAME = "INCOME_102";

    /**
     * Получение записей справочника
     *
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Перегруженный метод с восходящей сортировкой по умолчанию
     *
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     * @param filter условие фильтрации строк. Может быть не задано
     * @return
     */
    List<Long> getUniqueRecordIds(String filter);
    /**
     * Получает количетсво уникальных записей, удовлетворяющих условиям фильтра
     * @param filter условие фильтрации строк. Может быть не задано
     * @return
     */
    int getRecordsCount(String filter);
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
     * Обновление записей справочника
     * @param records
     */
    void updateRecords(List<Map<String, RefBookValue>> records);

	/**
	 * Удалить записи
	 * @param uniqueRecordIds идентификаторы записей
	 */
	void deleteRecords(List<Long> uniqueRecordIds);

    /**
     * Получает значения для справочника, находящиеся в отдельной таблице.
     * Наименование отчетного периода.
     * @param recordId значение для связанной таблицы
     * @return значение атрибута
     */
    String getPeriodNameFromRefBook(long recordId);
}
