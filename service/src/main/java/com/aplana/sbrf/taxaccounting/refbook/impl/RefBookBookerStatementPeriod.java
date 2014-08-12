package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBookerStatementPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributePair;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Провайдер для справочника "Периоды БО" (id = 108).
 */

@Service("refBookBookerStatementPeriod")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookBookerStatementPeriod extends AbstractReadOnlyRefBook {

    public static final Long REF_BOOK_ID = RefBookBookerStatementPeriodDao.REF_BOOK_ID;

    @Autowired
    RefBookBookerStatementPeriodDao bookerStatementPeriodDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PagingResult<Map<String, RefBookValue>> filteredRecords = bookerStatementPeriodDao.getRecords();
        if (pagingParams == null) {
            return filteredRecords;
        } else {
            PagingResult<Map<String, RefBookValue>> pagingResult = new PagingResult<Map<String, RefBookValue>>();
            int rightBound = Math.min(pagingParams.getStartIndex() + pagingParams.getCount(), filteredRecords.size());
            for (int i = pagingParams.getStartIndex(); i <= rightBound; ++i) {
                pagingResult.add(filteredRecords.get(i-1)); // индекс в пейджинге идет с 1, а не с 0
            }
            pagingResult.setTotalCount(filteredRecords.size());
            return pagingResult;
        }
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(@NotNull Long recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        throw new UnsupportedOperationException();
    }
}
