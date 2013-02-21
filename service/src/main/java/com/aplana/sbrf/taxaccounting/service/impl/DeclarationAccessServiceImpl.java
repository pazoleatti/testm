package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * @author dsultanbekov
 */
@Service
public class DeclarationAccessServiceImpl implements DeclarationAccessService {
	@Autowired
	TAUserDao userDao;
	
	@Autowired
	DeclarationTemplateDao declarationTemplateDao;
	
	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
	
	@Override
	public boolean canRead(int userId, long declarationId) {
		
		return true;
	}

	@Override
	public boolean canCreate(int userId, int declarationTemplateId, int departmentId, int reportPeriodId) {
		// Для начала проверяем, что в данном подразделении вообще можно работать с декларациями данного вида
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
		int declarationTypeId = declarationTemplate.getDeclarationType().getId();
		List<DepartmentDeclarationType> ddts = departmentDeclarationTypeDao.getDepartmentDeclarationTypes(departmentId);
		boolean found = false;
		for (DepartmentDeclarationType ddt: ddts) {
			if (ddt.getDeclarationTypeId() == declarationTypeId) {
				found = true;
				break;
			}
		}
		if (!found) {
			return false;
		}
		
		TAUser user = userDao.getUser(userId);
		// Контролёр УНП может создавать декларации в любом подразделении
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			return true;
		}
		
		// Обычный контролёр тоже может создавать декларации, но только в своём подразделении
		if (user.hasRole(TARole.ROLE_CONTROL) && user.getDepartmentId() == departmentId) {
			return true;
		}
		
		// В противном случае пользователь не имеет прав на создание налоговых форм
		return false;
	}

	@Override
	public boolean canAccept(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}

	@Override
	public boolean canReject(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}

	@Override
	public boolean canDelete(int userId, long declarationId) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canDownloadXml(int userId, long declarationId) {
		// TODO Auto-generated method stub
		return true;
	}
}
