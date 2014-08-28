package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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
    private PeriodService periodService;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private LockDataService lockDataService;

    private static final String RP_NAME_PATTERN = "%s %s";

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
                LogSystem log = new LogSystem();
                log.setLogDate(new Date());
                log.setIp(userInfo.getIp());
                log.setEventId(event.getCode());
                log.setUserLogin(userInfo.getUser().getLogin());

                StringBuilder roles = new StringBuilder();
                List<TARole> taRoles = userInfo.getUser().getRoles();
                for (int i = 0; i < taRoles.size(); i++) {
                    roles.append(taRoles.get(i).getName());
                    if (i != taRoles.size() - 1) {
                        roles.append(", ");
                    }
                }
                log.setRoles(roles.toString());

                String departmentName = departmentId == null ? "" : (departmentId == 0 ? departmentService.getDepartment(departmentId).getName() : departmentService.getParentsHierarchy(departmentId));
                log.setFormDepartmentName(departmentName.substring(0, Math.min(departmentName.length(), 2000)));
                log.setFormDepartmentId(departmentId);
                if (departmentId != null && departmentId != 0) log.setDepartmentTBId(departmentService.getParentTB(departmentId).getId());

                if (reportPeriodId == null)
                    log.setReportPeriodName(null);
                else {
                    ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
                    log.setReportPeriodName(String.format(RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName()));
                }
                log.setDeclarationTypeName(declarationTypeName);
                log.setFormTypeName(formTypeName);
                log.setFormKindId(formKindId);
                log.setNote(note != null ? note.substring(0, Math.min(note.length(), 2000)) : null);
                int userDepId = userInfo.getUser().getDepartmentId();
                String userDepartmentName = userDepId == 0 ? departmentService.getDepartment(userDepId).getName() : departmentService.getParentsHierarchy(userDepId);
                log.setUserDepartmentName(userDepartmentName.substring(0, Math.min(userDepartmentName.length(), 2000)));

                log.setBlobDataId(blobDataId);

                auditDao.add(log);
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
