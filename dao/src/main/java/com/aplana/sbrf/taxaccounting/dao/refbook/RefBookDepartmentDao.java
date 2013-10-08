package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * User: ekuvshinov
 */
public interface RefBookDepartmentDao {

	static final Long REF_BOOK_ID = 30L;

    /**
     * Загружает данные справочника
     * в данном случае даты актуальности нет смотри SBRFACCTAX-3245
     *
     * @param pagingParams
     * @param sortAttribute может быть не задан (null)
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * По коду возвращает строку справочника
     *
     *
     * @param recordId код строки справочника
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long recordId);
}
