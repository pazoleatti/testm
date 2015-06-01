package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DataRowServiceImpl implements DataRowService {
	
	@Autowired
    private LockDataService lockDataService;
	
	@Autowired
	private DataRowDao dataRowDao;
	
	@Autowired 
	private FormDataDao formDataDao;

    @Autowired
    private FormDataService formDataService;

	@Override
	public PagingResult<DataRow<Cell>> getDataRows(long formDataId, DataRowRange range, boolean saved, boolean manual) {
		PagingResult<DataRow<Cell>> result = new PagingResult<DataRow<Cell>>();
		FormData formData = formDataDao.get(formDataId, manual);
        result.addAll(saved ? dataRowDao.getSavedRows(formData, range) : dataRowDao.getTempRows(formData, range));
        result.setTotalCount(saved ? dataRowDao.getSavedSize(formData) : dataRowDao.getTempSize(formData));
		return result;
	}

    @Override
    public List<DataRow<Cell>> getSavedRows(FormData formData) {
        return dataRowDao.getSavedRows(formData, null);
    }

    @Override
	public int getRowCount(long formDataId, boolean saved, boolean manual) {
		FormData fd = formDataDao.get(formDataId, manual);
		return saved ? dataRowDao.getSavedSize(fd) : dataRowDao.getTempSize(fd);
	}

	@Override
	@Transactional(readOnly = false)
	public void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows, boolean manual) {
        checkLockedMe(lockDataService.getLock(formDataService.generateTaskKey(formDataId, ReportType.EDIT_FD)), userInfo.getUser());
		if ((dataRows != null) && (!dataRows.isEmpty())) {
			FormData fd = formDataDao.get(formDataId, manual);
			dataRowDao.updateRows(fd, dataRows);
		}
	}

    @Override
    @Transactional(readOnly = false)
    public void saveRows(FormData formData, List<DataRow<Cell>> dataRows) {
        dataRowDao.saveRows(formData, dataRows);
    }

    @Override
    public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive) {
        return dataRowDao.searchByKey(formDataId, formTemplateId, range, key, isCaseSensitive);
    }

    @Override
    @Transactional(readOnly = false)
    public void copyRows(long formDataSourceId, long formDataDestinationId) {
        dataRowDao.copyRows(formDataSourceId, formDataDestinationId);
    }

    @Override
    public void createTemporary(FormData formData) {
        formDataService.checkLockedByTask(formData.getId(), new Logger());
        dataRowDao.createTemporary(formData);
    }

    private void checkLockedMe(LockData lockData, TAUser user){
        if (lockData.getUserId() != user.getId()) {
            throw new ServiceException("Объект не заблокирован текущим пользователем");
        }
    }
}
