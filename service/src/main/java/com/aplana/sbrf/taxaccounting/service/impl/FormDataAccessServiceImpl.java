package com.aplana.sbrf.taxaccounting.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;

@Service
// TODO: добавить учёт ролей пользователя, но для прототипа достаточно только привязки к депаратаментам
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
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		// Контролёр уровня "Банк" имеет доступ ко всем сводным формам
		if (userDepartment.getType() == DepartmentType.ROOT_BANK) {
			return true;
		} else {
			// В противном случае доступ на чтение имеют только пользователи того же подразделения 
			return user.getDepartmentId() == formData.getDepartmentId();
		}
	}

	@Override
	public boolean canEdit(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);
		FormData formData = formDataDao.get(formDataId);
		return canEdit(user, formData);
	}
	
	private boolean canEdit(TAUser user, FormData formData) {
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		
		WorkflowState state = formData.getState();
		
		if (userDepartment.getType() == DepartmentType.ROOT_BANK) {
			return (state == WorkflowState.CREATED && formData.getDepartmentId() == userDepartment.getId())
				|| (state == WorkflowState.CREATED || state == WorkflowState.APPROVED && formData.getDepartmentId() != userDepartment.getId());
		} else {
			return (state == WorkflowState.CREATED && formData.getDepartmentId() == user.getDepartmentId());
		}
	}

	@Override
	public boolean canCreate(int userId, int formId, FormDataKind kind, int departmentId) {
		TAUser user = userDao.getUser(userId);
		if (departmentId == user.getDepartmentId()) {
			return true;
		}
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		return userDepartment.getType() == DepartmentType.ROOT_BANK && user.hasRole(TARole.ROLE_CONTROL);
	}

	@Override
	public boolean canDelete(int userId, long formDataId) {
		FormData formData = formDataDao.get(formDataId);
		TAUser user = userDao.getUser(userId);
		if (formData.getState() != WorkflowState.CREATED) {
			return false;
		}
		return canEdit(user, formData);
	}
}