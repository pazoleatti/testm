package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.service.DataRowService;

@Service
@Transactional(readOnly = true)
public class DataRowServiceImpl implements DataRowService {
	
	@Autowired
	private DataRowDao dataRowDao;
	
	@Autowired 
	private FormDataDao formDataDao;

	@Override
	public PaginatedSearchResult<DataRow<Cell>> getDataRows(
			TAUserInfo userInfo, long formDataId, DataRowRange range,
			boolean saved) {
		PaginatedSearchResult<DataRow<Cell>> result = new PaginatedSearchResult<DataRow<Cell>>();
		FormData fd = formDataDao.get(formDataId);
		result.setRecords(saved ? dataRowDao.getSavedRows(fd, null, range) : dataRowDao.getRows(fd, null, range));
		result.setTotalRecordCount(saved ? dataRowDao.getSavedSize(fd, null) : dataRowDao.getSize(fd, null));
		return result;
	}

	@Override
	public int getRowCount(TAUserInfo userInfo, long formDataId, boolean saved) {
		FormData fd = formDataDao.get(formDataId);
		return saved ? dataRowDao.getSavedSize(fd, null) : dataRowDao.getSize(fd, null);
	}

	@Override
	@Transactional(readOnly = false)
	public void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows) {
		FormData fd = formDataDao.get(formDataId);
		dataRowDao.updateRows(fd, dataRows);
	}

}
