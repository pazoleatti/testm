package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * User: ekuvshinov
 */
public interface RefBookDepartmentDao {
    /**
     * Загружает данные справочника
     * в данном случае даты актуальности нет смотри SBRFACCTAX-3245
     * TODO Добавить фильтрацию blocked by SBRFACCTAX-3187
     * TODO Добавить сортировку по атрибуту (пока не понятно как должно работать)
     *
     * @param refBookId
     * @param pagingParams
     * @param sortAttribute может быть не задан (null)
     * @return
     */
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, PagingParams pagingParams, RefBookAttribute sortAttribute);

    /**
     * По коду возвращает строку справочника
     *
     *
     * @param refBookId
     * @param recordId код строки справочника
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);
}
