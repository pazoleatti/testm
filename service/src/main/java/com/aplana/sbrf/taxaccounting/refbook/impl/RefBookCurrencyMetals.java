package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Провайдер для справочника "Коды валют и драгоценных металлов"
 * @author lhaziev
 */
@Service("RefBookCurrencyMetals")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookCurrencyMetals extends AbstractRefBookDataProvider {

    public static final Long REF_BOOK_ID = 542L;
    public static final Long REF_BOOK_CURRENCY_ID = 15L;
    public static final Long REF_BOOK_METALS_ID = 17L;

    @Autowired
    private RefBookFactory refBookFactory;

    private enum RefBookType {
        CURRENCY(0),
        METALS(1);

        private long deviation;

        public long getDeviation() {
            return deviation;
        }

        RefBookType(long deviation) {
            this.deviation = deviation;
        }

        public static RefBookType getByRefBookId(Long refBookId) {
            if (REF_BOOK_CURRENCY_ID.equals(refBookId)) {
                return CURRENCY;
            } else if (REF_BOOK_METALS_ID.equals(refBookId)) {
                return METALS;
            }
            return null;
        }
    }
    private RefBookDataProvider refBookDataProviderCurrency;
    private RefBookDataProvider refBookDataProviderMetals;
    private RefBook refBook;
    private RefBook refBookCurrency;
    private RefBook refBookMetals;

    void init() {
        if (refBookDataProviderCurrency == null)
            refBookDataProviderCurrency = refBookFactory.getDataProvider(REF_BOOK_CURRENCY_ID);
        if (refBookDataProviderMetals == null)
            refBookDataProviderMetals = refBookFactory.getDataProvider(REF_BOOK_METALS_ID);
        refBook = refBookFactory.get(REF_BOOK_ID);
        refBookCurrency = refBookFactory.get(REF_BOOK_CURRENCY_ID);
        refBookMetals = refBookFactory.get(REF_BOOK_METALS_ID);
    }

    private Map<String, RefBookValue> convertCurrency(Map<String, RefBookValue> record) {
        Map<String, RefBookValue> newRecord = new HashMap<String, RefBookValue>();
        newRecord.put("NAME", record.get("NAME"));
        newRecord.put("CODE", record.get("CODE"));
        newRecord.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, convertId(REF_BOOK_CURRENCY_ID, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue())));
        if (record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS))
            newRecord.put(RefBook.RECORD_VERSION_FROM_ALIAS, record.get(RefBook.RECORD_VERSION_FROM_ALIAS));
        if (record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS))
            newRecord.put(RefBook.RECORD_VERSION_TO_ALIAS, record.get(RefBook.RECORD_VERSION_TO_ALIAS));
        return newRecord;
    }

    private Map<String, RefBookValue> convertMetals(Map<String, RefBookValue> record) {
        Map<String, RefBookValue> newRecord = new HashMap<String, RefBookValue>();
        newRecord.put("NAME", record.get("NAME"));
        newRecord.put("CODE", record.get("INNER_CODE"));
        newRecord.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, convertId(REF_BOOK_METALS_ID, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue())));
        if (record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS))
            newRecord.put(RefBook.RECORD_VERSION_FROM_ALIAS, record.get(RefBook.RECORD_VERSION_FROM_ALIAS));
        if (record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS))
            newRecord.put(RefBook.RECORD_VERSION_TO_ALIAS, record.get(RefBook.RECORD_VERSION_TO_ALIAS));
        return newRecord;
    }

    private RefBookAttribute getAttribute(RefBookAttribute sortAttribute, RefBookType refBookType) {
        if (sortAttribute==null)
            return null;
        if (RefBookType.CURRENCY.equals(refBookType))
            return refBookCurrency.getAttribute(sortAttribute.getAlias());
        else
            return refBookMetals.getAttribute(sortAttribute.getAlias().equals("CODE")?"INNER_CODE":sortAttribute.getAlias());
    }

    private String getFilter(String filter, RefBookType refBookType) {
        if (filter == null || filter.isEmpty())
            return filter;
        if (RefBookType.CURRENCY.equals(refBookType))
            return filter;
        else
            return filter.replaceAll("CODE", "INNER_CODE");
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, final RefBookAttribute sortAttribute, boolean isSortAscending) {
        init();
        List<Map<String, RefBookValue>> result = new ArrayList<Map<String, RefBookValue>>();
        PagingParams newPagingParams = null;
        if (pagingParams != null) {
            newPagingParams = new PagingParams();
            newPagingParams.setStartIndex(1);
            newPagingParams.setCount(pagingParams.getCount() + pagingParams.getStartIndex() - 1);
        }
        PagingResult<Map<String, RefBookValue>> currencyPage = refBookDataProviderCurrency.getRecords(version, newPagingParams, getFilter(filter, RefBookType.CURRENCY), getAttribute(sortAttribute, RefBookType.CURRENCY), isSortAscending);
        for(Map<String, RefBookValue> record: currencyPage) {
            result.add(convertCurrency(record));
        }
        PagingResult<Map<String, RefBookValue>> metalsPage = refBookDataProviderMetals.getRecords(version, newPagingParams, getFilter(filter, RefBookType.METALS), getAttribute(sortAttribute, RefBookType.METALS), isSortAscending);
        for(Map<String, RefBookValue> record: metalsPage) {
            result.add(convertMetals(record));
        }
        if (sortAttribute != null) {
            Collections.sort(result, new Comparator<Map<String, RefBookValue>>() {
                @Override
                public int compare(Map<String, RefBookValue> o1, Map<String, RefBookValue> o2) {
                    String s1 = o1.get(sortAttribute.getAlias()).getStringValue();
                    String s2 = o2.get(sortAttribute.getAlias()).getStringValue();
                    if (s1 == null && s2 == null)
                        return 0;
                    if (s1 == null)
                        return -1;
                    if (s2 == null)
                        return 1;
                    return s1.compareTo(s2.toString());
                }
            });
        }
        if (newPagingParams == null)
            return new PagingResult<Map<String, RefBookValue>>(result, currencyPage.getTotalCount() + metalsPage.getTotalCount());
        return new PagingResult<Map<String, RefBookValue>>(
                result.subList(pagingParams.getStartIndex() - 1, Math.min(pagingParams.getStartIndex() + pagingParams.getCount() - 1, result.size())),
                currencyPage.getTotalCount() + metalsPage.getTotalCount());
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextVersion(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getEndVersion(Long recordId, Date versionFrom) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        init();
        List<Long> result = new ArrayList<Long>();
        for(Long id: refBookDataProviderCurrency.getUniqueRecordIds(version, filter)) {
            result.add(convertId(REF_BOOK_CURRENCY_ID, id));
        }
        for(Long id: refBookDataProviderMetals.getUniqueRecordIds(version, filter)) {
            result.add(convertId(REF_BOOK_METALS_ID, id));
        }
        return result;
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        init();
        return refBookDataProviderCurrency.getRecordsCount(version, getFilter(filter, RefBookType.CURRENCY)) +
                    refBookDataProviderMetals.getRecordsCount(version, getFilter(filter, RefBookType.METALS));
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		throw new UnsupportedOperationException();
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PagingResult<Map<String, RefBookValue>> paging = getRecords(version, null, filter, sortAttribute, isSortAscending);
        for(int i=0; i<paging.size(); i++)
            if (paging.get(i).get(RefBook.RECORD_ID_ALIAS).equals(recordId))
                return new Long(i)+1L;
        return null;
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        init();
        Long recordIdReal = recordId/10;
        long type = recordId % 10;
        if (RefBookType.CURRENCY.getDeviation() == type)
            return convertCurrency(refBookDataProviderCurrency.getRecordData(recordIdReal));
        return convertMetals(refBookDataProviderMetals.getRecordData(recordIdReal));
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        init();
        Map<Long, Map<String, RefBookValue>> result = new HashMap<Long, Map<String, RefBookValue>>();
        for(Map.Entry<Long, Map<String, RefBookValue>> entry: refBookDataProviderCurrency.getRecordData(recordIds).entrySet()) {
            result.put(convertId(REF_BOOK_CURRENCY_ID, entry.getKey()), convertCurrency(entry.getValue()));
        }
        for(Map.Entry<Long, Map<String, RefBookValue>> entry: refBookDataProviderMetals.getRecordData(recordIds).entrySet()) {
            result.put(convertId(REF_BOOK_METALS_ID, entry.getKey()), convertMetals(entry.getValue()));
        }
        return result;
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        init();
        List<Date> result = new ArrayList<Date>();
        result.addAll(refBookDataProviderCurrency.getVersions(startDate, endDate));
        result.addAll(refBookDataProviderMetals.getVersions(startDate, endDate));
		return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, final RefBookAttribute sortAttribute) {
        init();
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
        Long recordIdReal = recordId/10;
        long type = recordId % 10;
        if (RefBookType.CURRENCY.getDeviation() == type) {
            PagingResult<Map<String, RefBookValue>> currencyPage = refBookDataProviderCurrency.getRecordVersionsByRecordId(recordIdReal, pagingParams, getFilter(filter, RefBookType.CURRENCY), getAttribute(sortAttribute, RefBookType.CURRENCY));
            for(Map<String, RefBookValue> record: currencyPage) {
                result.add(convertCurrency(record));
            }
            result.setTotalCount(currencyPage.getTotalCount());
            return result;
        }
        PagingResult<Map<String, RefBookValue>> metalsPage = refBookDataProviderMetals.getRecordVersionsByRecordId(recordIdReal, pagingParams, getFilter(filter, RefBookType.METALS), getAttribute(sortAttribute, RefBookType.METALS));
        for(Map<String, RefBookValue> record: metalsPage) {
            result.add(convertMetals(record));
        }
        result.setTotalCount(metalsPage.getTotalCount());
        return result;
    }

    @Override
    public void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
		throw new UnsupportedOperationException();
    }

    @Override
    public void insertRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        init();
        RefBookRecordVersion result;
        Long recordIdReal = uniqueRecordId/10;
        long type = uniqueRecordId % 10;
        if (RefBookType.CURRENCY.getDeviation() == type) {
            result = refBookDataProviderCurrency.getRecordVersionInfo(recordIdReal);
            result.setRecordId(convertId(REF_BOOK_CURRENCY_ID, result.getRecordId()));
            return result;
        }
        result = refBookDataProviderMetals.getRecordVersionInfo(recordIdReal);
        result.setRecordId(convertId(REF_BOOK_METALS_ID, result.getRecordId()));
        return result;
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordVersionsCount(Long refBookRecordId) {
        init();
        Long recordIdReal = refBookRecordId/10;
        long type = refBookRecordId % 10;
        if (RefBookType.CURRENCY.getDeviation() == type)
            return refBookDataProviderCurrency.getRecordVersionsCount(recordIdReal);
        return refBookDataProviderMetals.getRecordVersionsCount(recordIdReal);
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEndWithoutLock(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        deleteRecordVersions(logger, uniqueRecordIds);
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        init();
        Long recordIdReal = uniqueRecordId/10;
        long type = uniqueRecordId % 10;
        if (RefBookType.CURRENCY.getDeviation() == type)
            return convertId(REF_BOOK_CURRENCY_ID, refBookDataProviderCurrency.getRecordId(recordIdReal));
        return convertId(REF_BOOK_METALS_ID, refBookDataProviderMetals.getRecordId(recordIdReal));
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        init();
        List<Long> realRecordIds = new ArrayList<Long>();
        Map<Long, Long> recordIdsMap = new HashMap<Long, Long>();
        for (Long fakeRecordId : recordIds) {
            long realRecordId = fakeRecordId / 10;
            realRecordIds.add(realRecordId);
            recordIdsMap.put(realRecordId, fakeRecordId);
        }
        List<ReferenceCheckResult> inactiveRecords = refBookDataProviderCurrency.getInactiveRecordsInPeriod(realRecordIds, periodFrom, periodTo);
        for(ReferenceCheckResult checkResult : inactiveRecords) {
            checkResult.setRecordId(recordIdsMap.get(checkResult.getRecordId()));
        }
        return inactiveRecords;
    }

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
        init();
        List<Long> currencyRecordIds = new ArrayList<Long>();
        List<Long> metalsRecordIds = new ArrayList<Long>();
        for(Long recordId: recordIds) {
            Long recordIdReal = recordId/10;
            long type = recordId % 10;
            if (RefBookType.CURRENCY.getDeviation() == type)
                currencyRecordIds.add(recordIdReal);
            else
                metalsRecordIds.add(recordIdReal);
        }
        Map<Long, RefBookValue> result = new HashMap<Long, RefBookValue>();
        for (Map.Entry<Long, RefBookValue> entry: refBookDataProviderCurrency.dereferenceValues(getAttribute(refBook.getAttribute(attributeId), RefBookType.CURRENCY).getId(), currencyRecordIds).entrySet()){
            result.put(convertId(REF_BOOK_CURRENCY_ID, entry.getKey()), entry.getValue());
        }
        for (Map.Entry<Long, RefBookValue> entry: refBookDataProviderMetals.dereferenceValues(getAttribute(refBook.getAttribute(attributeId), RefBookType.METALS).getId(), metalsRecordIds).entrySet()){
            result.put(convertId(REF_BOOK_METALS_ID, entry.getKey()), entry.getValue());
        }
        return result;
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> convertIds(Long refBookId, List<Long> ids) {
        List<Long> converted = new ArrayList<Long>(ids.size());
        for (Long id : ids) {
            converted.add(convertId(refBookId, id));
        }
        return converted;
    }

    private Long convertId(Long refBookId, Long id) {
        RefBookType type = RefBookType.getByRefBookId(refBookId);
        return type == null ? id : id * 10L + type.getDeviation();
    }
}
