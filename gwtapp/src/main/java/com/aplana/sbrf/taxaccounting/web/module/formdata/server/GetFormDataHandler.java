package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormDataWorkflowService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AccessFlags;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetFormDataHandler extends AbstractActionHandler<GetFormData, GetFormDataResult>{

	@Autowired
	private FormDataAccessService accessService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private ReportPeriodDao reportPeriodDao;
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private FormDataWorkflowService workflowService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		GetFormDataResult result = new GetFormDataResult();

		FormData formData = formDataService.getFormData(userId, action.getFormDataId());
		result.setFormData(formData);
		result.setDepartmenName(departmentDao.getDepartment(user.getDepartmentId()).getName());
		result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
		Long formDataId = formData.getId();
		AccessFlags accessFlags = new AccessFlags();
		accessFlags.setCanCreate(false);
		accessFlags.setCanDelete(accessService.canDelete(userId, formDataId));
		accessFlags.setCanEdit(accessService.canEdit(userId, formDataId));
		accessFlags.setCanRead(accessService.canRead(userId, formDataId));
		result.setAccessFlags(accessFlags);
		List<WorkflowMove> availableMoves =	workflowService.getAvailableMoves(userId, action.getFormDataId());
		result.setAvailableMoves(availableMoves);
		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
