package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Универсальный провайдер данных для справочников, хранящихся в отдельных таблицах. Только для чтения и без версионирования.
 */
@Service("refBookSimpleReadOnly")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookSimpleReadOnly extends AbstractReadOnlyRefBook {

    @Autowired
    private RefBookSimpleDao refBookSimpleDao;

    @Autowired
    private CommonRefBookService commonRefBookService;

	/** Дополнительная фильтрация выборки */
	private String whereClause;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, String direction) {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecordsWithVersionInfo(getRefBook(), version, pagingParams, filter, sortAttribute, direction);
        return commonRefBookService.dereference(getRefBook(), records);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
			RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookDao.getRecords(getRefBookId(), getTableName(), pagingParams, filter, sortAttribute, isSortAscending, getWhereClause());
    }

	@Override
	public PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter) {
		return getRecords(versionTo, pagingParams, filter, null);
	}

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getChildrenRecords(getRefBookId(), getTableName(), parentRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(getRefBookId(), getTableName(), recordId, filter, sortAttribute, isSortAscending, getWhereClause());
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(getRefBookId(), getTableName(), recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        return refBookSimpleDao.getRecordData(refBookDao.get(getRefBookId()), recordIds);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataWhere(String where) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataVersionWhere(String where, Date version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return refBookDao.getUniqueRecordIds(getRefBookId(), getTableName(), filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookSimpleDao.getRecordsCount(refBookDao.get(getRefBookId()), version, filter);
    }

    public String getTableName() {
		return refBook.getTableName();
	}

	public String getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		return refBookDao.dereferenceValues(getTableName(), attributeId, recordIds);
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecords(getTableName(), recordIds);
    }
}