package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
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
	private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private FormDataAccessService formDataAccessService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private TransactionHelper tx;


    @Override
	public PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		return auditDao.getLogs(filter);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                LogSystem log = new LogSystem();
                log.setLogDate(new Date());
                log.setIp(userInfo.getIp());
                log.setEventId(event.getCode());
                log.setUserId(userInfo.getUser().getId());

                StringBuilder roles = new StringBuilder();
                List<TARole> taRoles = userInfo.getUser().getRoles();
                for (int i = 0; i < taRoles.size(); i++) {
                    roles.append(taRoles.get(i).getName());
                    if (i != taRoles.size() - 1) {
                        roles.append(", ");
                    }
                }
                log.setRoles(roles.toString());

                String departmentName = departmentId == null ? "" : departmentService.getParentsHierarchy(departmentId);
                log.setFormDepartmentName(departmentName.substring(0, Math.min(departmentName.length(), 2000)));
                log.setFormDepartmentId(departmentId);
                if (reportPeriodId == null)
                    log.setReportPeriodName(null);
                else {
                    ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
                    log.setReportPeriodName(String.format(AuditService.RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName()));
                }
                log.setDeclarationTypeName(declarationTypeName);
                log.setFormTypeName(formTypeName);
                log.setFormKindId(formKindId);
                log.setNote(note != null ? note.substring(0, Math.min(note.length(), 2000)) : null);
                String userDepartmentName = departmentService.getParentsHierarchy(userInfo.getUser().getDepartmentId());
                log.setUserDepartmentName(userDepartmentName.substring(0, Math.min(userDepartmentName.length(), 2000)));

                auditDao.add(log);
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });            
	}

	@Override
	public LogSystemFilterAvailableValues getFilterAvailableValues(TAUser user) {
		LogSystemFilterAvailableValues values = new LogSystemFilterAvailableValues();
        if (user.hasRole(TARole.ROLE_ADMIN) || user.hasRole(TARole.ROLE_CONTROL_UNP))
            values.setDepartments(departmentService.listAll());
        else if (user.hasRole(TARole.ROLE_CONTROL_NS)){
            List<Integer> departments = departmentService.getTaxFormDepartments(user, Arrays.asList(TaxType.values()));
            if (departments.isEmpty()){
                values.setDepartments(new ArrayList<Department>());
            } else{
                Set<Integer> departmentIds = new HashSet<Integer>(departments);
                values.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(departmentIds).values()));
            }
        }

		/*values.setFormTypeIds(formTypeDao.getAll());*/
		values.setDeclarationTypes(declarationTypeDao.listAll());
		return values;
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
        add(FormDataEvent.LOG_SYSTEM_BACKUP, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, "Архивация ЖА");
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
        List<Integer> departments = departmentService.getTaxFormDepartments(userInfo.getUser(), asList(TaxType.values()));
        try {
            return auditDao.getLogsBusiness(filter, departments);
        } catch (DaoException e){
            throw new ServiceException("Поиск по НФ/декларациям.", e);
        }
    }
}
