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

import static com.aplana.sbrf.taxaccounting.dao.AuditDao.SAMPLE_NUMBER;

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
		return auditDao.getLogsForAdmin(filter);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note, final String blobDataId, final Integer formTypeId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                commonService.addAuditLog(event, userInfo, departmentId, reportPeriodId,
                        declarationTypeName, formTypeName, formKindId, note, blobDataId, formTypeId);
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
        add(FormDataEvent.LOG_SYSTEM_BACKUP, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, "Архивация ЖА", null, null);
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
        try {
            TAUser user = userInfo.getUser();
            if (user.hasRole(TARole.ROLE_ADMIN)) {
                return auditDao.getLogsForAdmin(filter);
            } else if (user.hasRole(TARole.ROLE_CONTROL_NS) || user.hasRole(TARole.ROLE_CONTROL)) {
                HashMap<SAMPLE_NUMBER, Collection<Integer>> sampleVal =
                        new HashMap<SAMPLE_NUMBER, Collection<Integer>>(3);
                sampleVal.put(SAMPLE_NUMBER.S_10, departmentService.getBADepartmentIds(userInfo.getUser()));
                sampleVal.put(SAMPLE_NUMBER.S_45, departmentService.getSourcesDepartmentIds(userInfo.getUser(), null, null));
                sampleVal.put(SAMPLE_NUMBER.S_55, departmentService.getAppointmentDepartments(user));

                return auditDao.getLogsBusinessForControl(filter, sampleVal);
            } else if (user.hasRole(TARole.ROLE_OPER)) {
                HashMap<SAMPLE_NUMBER, Collection<Integer>> sampleVal =
                        new HashMap<SAMPLE_NUMBER, Collection<Integer>>(2);
                sampleVal.put(SAMPLE_NUMBER.S_10, departmentService.getBADepartmentIds(userInfo.getUser()));
                sampleVal.put(SAMPLE_NUMBER.S_55, departmentService.getAppointmentDepartments(user));
                return auditDao.getLogsBusinessForOper(filter, sampleVal);
            } else if (user.hasRole(TARole.ROLE_CONTROL_UNP)){
                return auditDao.getLogsBusinessForControlUnp(filter);
            }
        } catch (DaoException e) {
            throw new ServiceException("Поиск по НФ/декларациям.", e);
        }


        return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(0));
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
