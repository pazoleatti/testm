package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.impl.AbstractReadOnlyRefBook;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Справочник созданный на основе перечислений
 * в реализации игнорируются: версия, фильтр, сортировка
 *
 * @author auldanov on 05.02.14.
 */
public abstract class AbstractPermanentRefBook extends AbstractReadOnlyRefBook {

    /**
     * Метод возвращает данные из справочника (id записи, карту алиасов и значений) с учетом фильтрации
     * @return Map<recordId, Map<Alias, Value>>
     */
    abstract protected PagingResult<Map<String, RefBookValue>> getRecords(String filter);

    @Override
    public Date getNextVersion(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * В реализации игнорируются: версия, сортировка
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		PagingResult<Map<String, RefBookValue>> filteredRecords = getRecords(filter);
		if (pagingParams == null) {
			return filteredRecords;
		} else {
			PagingResult<Map<String, RefBookValue>> pagingResult = new PagingResult<Map<String, RefBookValue>>();
			int rightBound = Math.min(pagingParams.getStartIndex() + pagingParams.getCount(), filteredRecords.size());
			for (int i = pagingParams.getStartIndex(); i <= rightBound; ++i) {
				pagingResult.add(filteredRecords.get(i-1)); // индекс в пейджинге идет с 1, а не с 0
			}
			pagingResult.setTotalCount(rightBound - pagingParams.getStartIndex() + 1);
			return pagingResult;
		}
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return getRecords(version, null, filter, null, true).size();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		PagingResult<Map<String, RefBookValue>> records = getRecords(null);
		for (Map<String, RefBookValue> record: records) {
			if (recordId.equals(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue())) {
				return record;
			}
		}
        throw new IllegalArgumentException("There does not exist a record with ID = " + recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, @NotNull Date periodTo) {
        throw new UnsupportedOperationException();
    }
}
