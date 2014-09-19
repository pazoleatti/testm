package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.common.service.CommonService;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;

@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

	@Autowired
	private AuditDao auditDao;
	@Autowired
	private DepartmentService departmentService;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private CommonService commonService;

    @Override
	public PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		return auditDao.getLogs(filter);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note, final String blobDataId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                commonService.addAuditLog(event, userInfo, departmentId, reportPeriodId,
                        declarationTypeName, formTypeName, formKindId, note, blobDataId);
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

    @Override
    @Transactional(readOnly = false)
    public void removeRecords(List<LogSearchResultItem> items, TAUserInfo userInfo) {
        if (!items.isEmpty()){
            List<Long> listIds = new ArrayList<Long>();
            for (LogSearchResultItem item : items)
                listIds.add(item.getId());
            auditDao.removeRecords(listIds);
        }
        add(FormDataEvent.LOG_SYSTEM_BACKUP, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, "Архивация ЖА", null);
    }

    @Override
    public Date getLastArchiveDate() {
        try {
            return auditDao.lastArchiveDate();
        } catch (DaoException e){
            throw new ServiceException("Ошибка при получении последней даты архивации.", e);
        }
    }

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilter filter, TAUserInfo userInfo) {
        List<Integer> departments = departmentService.getTaxFormDepartments(userInfo.getUser(), asList(TaxType.values()), null, null);
        List<Department> BADepartments = departmentService.getBADepartments(userInfo.getUser());
        List<Integer> BADepartmentIds = new ArrayList<Integer>();
        for(Department department: BADepartments) {
            BADepartmentIds.add(department.getId());
        }
        try {
            return auditDao.getLogsBusiness(filter, departments, BADepartmentIds);
        } catch (DaoException e){
            throw new ServiceException("Поиск по НФ/декларациям.", e);
        }
    }

    @Override
    public LockData lock(TAUserInfo userInfo) {
        return lockDataService.lock("LOG_SYSTEM_BACKUP", userInfo.getUser().getId(), 3600000);
    }

    @Override
    public void unlock(TAUserInfo userInfo) {
        lockDataService.unlock("LOG_SYSTEM_BACKUP", userInfo.getUser().getId());
    }
}
