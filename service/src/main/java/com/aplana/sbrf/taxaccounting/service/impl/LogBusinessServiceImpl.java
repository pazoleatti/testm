package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LogBusinessServiceImpl implements LogBusinessService {

	@Autowired
	private LogBusinessDao logBusinessDao;

	@Override
	public List<LogBusiness> getFormLogsBusiness(long formId) {
		return logBusinessDao.getFormLogsBusiness(formId);
	}

	@Override
	@Transactional(readOnly = false)
	public void add(Long formDataId, Long declarationId, TAUser user, FormDataEvent event, String note) {
		LogBusiness log = new LogBusiness();
		log.setFormId(formDataId);
		log.setDeclarationId(declarationId);
		log.setEventId(event.getCode());
		log.setUserId(user.getId());
		log.setLogDate(new Date());
		log.setNote(note);
		log.setDepartmentId(user.getDepartmentId());

		StringBuilder roles = new StringBuilder();
		for (TARole role : user.getRoles()) {
			roles.append(role.getName());
		}
		log.setRoles(roles.toString());

		logBusinessDao.add(log);
	}

	@Override
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId) {
		return logBusinessDao.getDeclarationLogsBusiness(declarationId);
	}
}
