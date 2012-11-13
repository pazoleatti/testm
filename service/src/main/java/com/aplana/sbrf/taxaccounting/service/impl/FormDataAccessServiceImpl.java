package com.aplana.sbrf.taxaccounting.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;

@Service
public class FormDataAccessServiceImpl implements FormDataAccessService {
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private DepartmentDao departmentDao;
	
	@Override
	public boolean canRead(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);
		FormData formData = formDataDao.get(formDataId);
		return canRead(user, formData);
	}

	private boolean canRead(TAUser user, FormData formData) {
		// TODO: черновая реализация: все пользователи могут просматривать любые формы
		return true;
	}

	@Override
	public boolean canEdit(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);
		FormData formData = formDataDao.get(formDataId);
		return canEdit(user, formData);
	}
	
	private boolean canEdit(TAUser user, FormData formData) {
		// TODO: черновая реализация: пользователь имеет доступ на редактирование 
		// ко всем формам, если у него роль "Контролёр" 
		return user.hasRole(TARole.ROLE_CONTROL);
	}

	@Override
	public boolean canCreate(int userId, int formId) {
		TAUser user = userDao.getUser(userId);
		Department department = departmentDao.getDepartment(user.getDepartmentId()); 
		// TODO: добавить проверку, что департамент пользователя может работать с данным типом форм
		return true;
	}
}