package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataWorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FormDataWorkflowServiceImpl implements FormDataWorkflowService {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDataAccessService formDataAccessService;
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private DepartmentDao departmentDao;

	@Override
	public List<WorkflowMove> getAvailableMoves(int userId, long formDataId) {
		List<WorkflowMove> result = new ArrayList<WorkflowMove>();
		// Для того, чтобы иметь возможность изменить статус, у пользователя должны быть права
		// на чтение соответствующей карточки данных
		if (!formDataAccessService.canRead(userId, formDataId)) {
			return result;
		}
		
		FormData formData = formDataDao.get(formDataId);
		WorkflowState state = formData.getState();
		TAUser user = userDao.getUser(userId);
		
		int formDataDepartmentId = formData.getDepartmentId();
		int userDepartmentId = user.getDepartmentId();
		Department formDataDepartment = departmentDao.getDepartment(formDataDepartmentId);
		Department userDepartment = departmentDao.getDepartment(userDepartmentId);
		
		
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

}
