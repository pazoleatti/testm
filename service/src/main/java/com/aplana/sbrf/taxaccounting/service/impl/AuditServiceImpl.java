package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

	@Autowired
	private AuditDao auditDao;
	@Autowired
	private DepartmentService departmentService;
    @Autowired
	private DeclarationTypeDao declarationTypeDao;

	@Override
	public PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		return auditDao.getLogs(filter);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
	public void add(FormDataEvent event, TAUserInfo userInfo, int departmentId, Integer reportPeriodId,
					Integer declarationTypeId, Integer formTypeId, Integer formKindId, String note) {
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

		log.setDepartmentId(departmentId);
		log.setReportPeriodId(reportPeriodId);
		log.setDeclarationTypeId(declarationTypeId);
		log.setFormTypeId(formTypeId);
		log.setFormKindId(formKindId);
		log.setNote(note);
		log.setUserDepartmentId(userInfo.getUser().getDepartmentId());

		auditDao.add(log);
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
}
