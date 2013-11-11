package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Универсальный провайдер данных
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:32
 */
@Service("refBookUniversal")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookUniversal implements RefBookDataProvider {

	@Autowired
	private RefBookDao refBookDao;

	protected Long refBookId;

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	@Override
	public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
			PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getChildrenRecords(refBookId, parentRecordId, version, pagingParams, filter, sortAttribute);
	}

	@Override
	public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getRecords(refBookId, version, pagingParams, filter, sortAttribute);
	}

	@Override
	public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(refBookId, recordId);
	}

	@Override
	public List<Date> getVersions(Date startDate, Date endDate) {
		return refBookDao.getVersions(refBookId, startDate, endDate);
	}

	@Override
	public void deleteRecords(Date version, List<Long> recordIds) {
        refBookDao.deleteRecords(refBookId, version, recordIds);
	}

    @Override
    public void deleteAllRecords(Date version) {
        refBookDao.deleteAllRecords(refBookId, version);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        return refBookDao.getValue(recordId, attributeId);
    }

    @Override
	public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.createRecords(refBookId, version, records);
	}

	@Override
	public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.updateRecords(refBookId, version, records);
	}
}
