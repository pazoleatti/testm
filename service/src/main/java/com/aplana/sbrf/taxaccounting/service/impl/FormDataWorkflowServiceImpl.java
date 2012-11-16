package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.WorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.Workflow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataWorkflowService;

public class FormDataWorkflowServiceImpl implements FormDataWorkflowService {
	@Autowired
	private WorkflowDao workflowDao;
	@Autowired
	private FormDataAccessService formDataAccessService;
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private FormDao formDao;

	@Override
	public List<WorkflowMove> getAvailableMoves(int userId, long formDataId) {
		FormData formData = formDataDao.get(formDataId);
		int currentStateId = formData.getStateId();
		Form form = formDao.getForm(formData.getFormTemplateId());
		Workflow wf = workflowDao.getWorkflow(form.getWorkflowId());		
		
		List<WorkflowMove> result = new ArrayList<WorkflowMove>();
		TAUser user = userDao.getUser(userId);  

		// TODO: проверки прав доступа на изменение статуса
		// Сейчас делается черновая реализация: пользователь с ролью "Контролёр"
		// может выполнять любые переходы по любой карточке данных, остальные - не могут этого делать
		if (!user.hasRole(TARole.ROLE_CONTROL)) {
			return new ArrayList<WorkflowMove>(0);
		}
		
		for (WorkflowMove m: wf.getMoves()) {
			if (m.getFromStateId() == currentStateId) {
				result.add(m);
			}
		}
		return result;
	}

	@Override
	public void doMove(long formDataId, int userId, int moveId) {

	}
}
