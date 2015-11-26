package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO для работы справочника пользователей
 * @author auldanov
 */
public interface RefBookUserDao {

	Long REF_BOOK_ID = 74L;
	String TABLE_NAME = "SEC_USER";

    /**
     * Вариант перегрузки, для обратной совместимости интерфейса
     *
     *
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

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
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return список идентификаторов
     */
    List<Long> getUniqueRecordIds(Date version, String filter);

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return количество
     */
    int getRecordsCount(Date version, String filter);

    /**
     * Получение записи справочника по recordId
     * @param recordId
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Получение row_num записи по заданным параметрам
     *
     * @param recordId
     * @param filter
     * @param sortAttribute
     * @return
     */
    Long getRowNum(Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending);

    /**
     * Проверяет действуют ли записи справочника в указанном периоде
     * @param recordIds уникальные идентификаторы записей справочника
     * @return список id записей при проверке которых были обнаружены ошибки + код ошибки
     */
    Map<Long, CheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds);
}
