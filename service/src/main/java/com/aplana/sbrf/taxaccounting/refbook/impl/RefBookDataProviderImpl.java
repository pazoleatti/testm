package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Универсальный провайдер данных
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:32
 */
@Service
public class RefBookDataProviderImpl implements RefBookDataProvider {

	@Autowired
	private RefBookDaoImpl refBookDao;

	@Override
	public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version,
			PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getChildrenRecords(refBookId, parentRecordId, version, pagingParams, filter, sortAttribute);
	}

	@Override
	public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getRecords(refBookId, version, pagingParams, filter, sortAttribute);
	}

	@Override
	public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
		return refBookDao.getRecordData(refBookId, recordId);
	}

	@Override
	public List<Date> getVersions(Long refBookId, Date startDate, Date endDate) {
		return refBookDao.getVersions(refBookId, startDate, endDate);
	}
}
