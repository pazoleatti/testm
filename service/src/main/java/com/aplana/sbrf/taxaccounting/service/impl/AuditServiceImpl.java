package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

	@Autowired
	private AuditDao auditDao;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private DeclarationTypeDao declarationTypeDao;

	@Override
	public PagingResult<LogSystemSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		if (filter.getFromSearchDate() == null || filter.getToSearchDate() == null) {
			throw new ServiceException("Необходимо ввести поисковые даты \"От\" и \"До\"");
		}
        if (filter.getFromSearchDate().compareTo(filter.getToSearchDate()) > 0)
            throw new ServiceException("Дата \"От\" должна быть меньше или равна дате \"До\"");

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
		for (TARole role : userInfo.getUser().getRoles()) {
			roles.append(role.getName());
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
	public LogSystemFilterAvailableValues getFilterAvailableValues() {
		LogSystemFilterAvailableValues values = new LogSystemFilterAvailableValues();
		values.setDepartments(departmentService.listDepartments());
		values.setFormTypes(formTypeDao.getAll());
		values.setDeclarationTypes(declarationTypeDao.listAll());
		return values;
	}

    @Override
    @Transactional(readOnly = false)
    public void removeRecords(List<LogSystemSearchResultItem> items, TAUserInfo userInfo) {
        if (!items.isEmpty()){
            List<Long> listIds = new ArrayList<Long>();
            for (LogSystemSearchResultItem item : items)
                listIds.add(item.getId());
            auditDao.removeRecords(listIds);
        }
        add(FormDataEvent.LOG_SYSTEM_BACKUP, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, "Архивация ЖА");
    }
}
