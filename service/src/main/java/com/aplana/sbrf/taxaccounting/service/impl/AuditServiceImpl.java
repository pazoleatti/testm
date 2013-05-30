package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

	@Autowired
	private AuditDao auditDao;

	@Override
	public List<LogSystem> getLogs(LogSystemFilter filter) {
		if (filter.getFromSearchDate() == null || filter.getToSearchDate() == null) {
			throw new ServiceException("Необходимо ввести поисковые даты \"От\" и \"До\"");
		}

		return auditDao.getLogs(filter);
	}

	@Override
	@Transactional(readOnly = false)
	public void add(String ip, FormDataEvent event, TAUser user, int departmentId, int reportPeriodId,
					Integer declarationTypeId, Integer formTypeId, Integer formKindId, String note) {
		LogSystem log = new LogSystem();
		log.setLogDate(new Date());
		log.setIp(ip);
		log.setEventId(event.getCode());
		log.setUserId(user.getId());

		StringBuilder roles = new StringBuilder();
		for (TARole role : user.getRoles()) {
			roles.append(role.getName());
		}
		log.setRoles(roles.toString());

		log.setDepartmentId(departmentId);
		log.setReportPeriodId(reportPeriodId);
		log.setDeclarationTypeId(declarationTypeId);
		log.setFormTypeId(formTypeId);
		log.setFormKindId(formKindId);
		log.setNote(note);
		log.setUserDepartmentId(user.getDepartmentId());

		auditDao.add(log);
	}
}
