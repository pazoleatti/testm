package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
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
    @Autowired
    private DepartmentService departmentService;

    @Override
	public List<LogBusiness> getFormLogsBusiness(long formId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting) {
		return logBusinessDao.getFormLogsBusiness(formId, ordering, isAscSorting);
	}

    @Override
	@Transactional(readOnly = false)
	public void add(Long formDataId, Long declarationId, TAUserInfo userInfo, FormDataEvent event, String note) {
		LogBusiness log = new LogBusiness();
		log.setFormId(formDataId);
		log.setDeclarationId(declarationId);
		log.setEventId(event.getCode());
		log.setUserLogin(userInfo.getUser().getLogin());
		log.setLogDate(new Date());
		log.setNote(note);

		log.setDepartmentName(departmentService.getParentsHierarchyShortNames(userInfo.getUser().getDepartmentId()));

		StringBuilder roles = new StringBuilder();
        List<TARole> taRoles = userInfo.getUser().getRoles();
        for (int i = 0; i < taRoles.size(); i++) {
            roles.append(taRoles.get(i).getName());
            if (i != taRoles.size() - 1) {
                roles.append(", ");
            }
        }
        log.setRoles(roles.toString());

		logBusinessDao.add(log);
	}

    @Override
    public String getUserLoginImportTf(long declarationDataId) {
        return logBusinessDao.getUserLoginImportTf(declarationDataId);
    }

    @Override
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting) {
		return logBusinessDao.getDeclarationLogsBusiness(declarationId, ordering, isAscSorting);
	}
}
