package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;

@Service
// TODO: добавить учёт ролей пользователя, но для прототипа достаточно только привязки к депаратаментам
public class FormDataAccessServiceImpl implements FormDataAccessService {
	private Log logger = LogFactory.getLog(getClass());	
	
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
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		return canRead(user, userDepartment, formData);
	}

	private boolean canRead(TAUser user, Department userDepartment, FormData formData) {
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
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());		
		FormData formData = formDataDao.get(formDataId);
		return canEdit(user, userDepartment, formData);
	}
	
	private boolean canEdit(TAUser user, Department userDepartment, FormData formData) {
		WorkflowState state = formData.getState();
		
		if (userDepartment.getType() == DepartmentType.ROOT_BANK) {
			return (state == WorkflowState.CREATED && formData.getDepartmentId() == userDepartment.getId())
				|| (state == WorkflowState.CREATED || state == WorkflowState.APPROVED && formData.getDepartmentId() != userDepartment.getId());
		} else {
			return (state == WorkflowState.CREATED && formData.getDepartmentId() == user.getDepartmentId());
		}
	}

	@Override
	public boolean canCreate(int userId, int formTemplateId, FormDataKind kind, int departmentId) {
		TAUser user = userDao.getUser(userId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		return canCreate(user, userDepartment, formTemplateId, kind, departmentId);
	}
	
	private boolean canCreate(TAUser user, Department userDepartment, int formTemplateId, FormDataKind kind, int departmentId) {
		if (departmentId == user.getDepartmentId()) {
			return true;
		}
		return userDepartment.getType() == DepartmentType.ROOT_BANK && user.hasRole(TARole.ROLE_CONTROL);
	}

	@Override
	public boolean canDelete(int userId, long formDataId) {
		FormData formData = formDataDao.get(formDataId);
		TAUser user = userDao.getUser(userId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		return canDelete(user, userDepartment, formData);
	}
	
	private boolean canDelete(TAUser user, Department userDepartment, FormData formData) {
		if (formData.getState() != WorkflowState.CREATED) {
			return false;
		}
		return canEdit(user, userDepartment, formData);
	}
	
	
	@Override
	public List<WorkflowMove> getAvailableMoves(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);		
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		FormData formData = formDataDao.get(formDataId);
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		return getAvailableMoves(user, userDepartment, formData, formDataDepartment);
	}
	
	private List<WorkflowMove> getAvailableMoves(TAUser user, Department userDepartment, FormData formData, Department formDataDepartment) {
		List<WorkflowMove> result = new ArrayList<WorkflowMove>();
		// Для того, чтобы иметь возможность изменить статус, у пользователя должны быть права
		// на чтение соответствующей карточки данных
		if (!canRead(user, userDepartment, formData)) {
			return result;
		}
		WorkflowState state = formData.getState();
		
		boolean isBankLevelFormData = formDataDepartment.getType() == DepartmentType.ROOT_BANK;
		boolean isBankLevelUser = userDepartment.getType() == DepartmentType.ROOT_BANK;
		
		switch (state) {
		case CREATED:
			if (isBankLevelFormData) {
				result.add(WorkflowMove.CREATED_TO_ACCEPTED);
			} else {
				result.add(WorkflowMove.CREATED_TO_APPROVED);
			}
			break;
		case PREPARED:
			// Для ЖЦ прототипа не требуется
			break;
		case APPROVED:
			if (isBankLevelFormData) {
				logger.warn("Bank-level formData couldn't be in APPROVED state!");
			} else {
				result.add(WorkflowMove.APPROVED_TO_CREATED);
				if (isBankLevelUser) {
					result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
				}
			}
			break;
		case ACCEPTED:
			if (isBankLevelFormData) {
				result.add(WorkflowMove.ACCEPTED_TO_CREATED);
			} else {
				if (isBankLevelUser) {
					result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
				}
			}
			break;
		}
		return result; 
	}

	@Override
	public FormDataAccessParams getFormDataAccessParams(int userId,	long formDataId) {
		TAUser user = userDao.getUser(userId);		
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		FormData formData = formDataDao.get(formDataId);
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		
		FormDataAccessParams result = new FormDataAccessParams();
		result.setCanRead(canRead(user, userDepartment, formData));
		result.setCanEdit(canEdit(user, userDepartment, formData));
		result.setCanDelete(canDelete(user, userDepartment, formData));
		result.setAvailableWorkflowMoves(getAvailableMoves(user, userDepartment, formData, formDataDepartment));
		return result;
	}
}