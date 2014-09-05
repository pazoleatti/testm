package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DataRowServiceImpl implements DataRowService {
	
	@Autowired
    LockDataService lockDataService;
	
	@Autowired
	private DataRowDao dataRowDao;
	
	@Autowired 
	private FormDataDao formDataDao;

	@Override
	public PagingResult<DataRow<Cell>> getDataRows(
            TAUserInfo userInfo, long formDataId, DataRowRange range,
            boolean saved, boolean manual) {
		PagingResult<DataRow<Cell>> result = new PagingResult<DataRow<Cell>>();
		FormData fd = formDataDao.get(formDataId, manual);
		result.addAll(saved ? dataRowDao.getSavedRows(fd, null, range) : dataRowDao.getRows(fd, null, range));
		result.setTotalCount(saved ? dataRowDao.getSavedSize(fd, null) : dataRowDao.getSize(fd, null));
		return result;
	}

	@Override
	public int getRowCount(TAUserInfo userInfo, long formDataId, boolean saved, boolean manual) {
		FormData fd = formDataDao.get(formDataId, manual);
		return saved ? dataRowDao.getSavedSize(fd, null) : dataRowDao.getSize(fd, null);
	}

	@Override
	@Transactional(readOnly = false)
	public void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows, boolean manual) {
        checkLockedMe(lockDataService.lock(LockData.LOCK_OBJECTS.FORM_DATA.name() + "_" + formDataId,
                userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME), userInfo.getUser());
		if ((dataRows != null) && (!dataRows.isEmpty())) {
			FormData fd = formDataDao.get(formDataId, manual);
			dataRowDao.updateRows(fd, dataRows);
		}
	}

	@Override
	public void rollback(TAUserInfo userInfo, long formDataId) {
        checkLockedMe(lockDataService.lock(LockData.LOCK_OBJECTS.FORM_DATA.name() + "_" + formDataId,
                userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME), userInfo.getUser());
		dataRowDao.rollback(formDataId);
	}

    @Override
    public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive) {
        return dataRowDao.searchByKey(formDataId, formTemplateId, range, key, isCaseSensitive);
    }

    private void checkLockedMe(LockData lockData, TAUser user){
        if (lockData.getUserId() != user.getId()) {
            throw new ServiceException("Объект не заблокирован текущим пользователем");
        }
    }
}
