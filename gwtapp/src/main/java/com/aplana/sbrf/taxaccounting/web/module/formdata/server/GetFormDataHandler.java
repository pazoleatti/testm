package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.util.ArrayList;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * TODO: Разделить класс на 2: показ и создание формы. Result можно оставить общий.
 */
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
	FormTemplateDao formTemplateDao;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException {
		checkAction(action);
		
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();

		FormData formData;
		if (action.getWorkFlowMove() != null) {
			formDataService.doMove(action.getFormDataId(), userId, action.getWorkFlowMove(), logger);
			logger.getEntries().add(new LogEntry(LogLevel.INFO, "OLOLO"));
		}
		if(action.getFormDataId() == Long.MAX_VALUE){
			formData = formDataService.createFormData(logger, userId, formTemplateDao.getActiveFormTemplateId(action.getFormDataTypeId().intValue()), action.getDepartmentId().intValue(),
					FormDataKind.fromId(action.getFormDataKind().intValue()));

			if(action.getReportPeriodId() != null){
				System.out.println("-----" + formData.getReportPeriodId()+ ":" + reportPeriodDao.get(action.getReportPeriodId().intValue()));
				result.setReportPeriod(reportPeriodDao.get(action.getReportPeriodId().intValue()).getName());
			}

			result.setDepartmenName(departmentDao.getDepartment(action.getDepartmentId()).getName());
			FormDataAccessParams accessParams = new FormDataAccessParams();
			accessParams.setCanDelete(false);
			accessParams.setCanEdit(true);
			accessParams.setCanRead(true);
			accessParams.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(0));
			result.setFormDataAccessParams(accessParams);
		}
		else{
			result.setLogEntries(new ArrayList<LogEntry>());

			formData = formDataService.getFormData(userId, action.getFormDataId());
			result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
			result.setDepartmenName(departmentDao.getDepartment(formData.getDepartmentId()).getName());
			Long formDataId = formData.getId();
			FormDataAccessParams accessParams = accessService.getFormDataAccessParams(userId, formDataId);
			result.setFormDataAccessParams(accessParams);
		}
		result.setLogEntries(logger.getEntries());
		result.setFormData(formData);
		
		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
	
	private void checkAction(GetFormData action){
		if(action.getFormDataId() == Long.MAX_VALUE && (action.getDepartmentId()==Long.MAX_VALUE || action.getFormDataKind()==Long.MAX_VALUE || action.getFormDataTypeId() == Long.MAX_VALUE))
			throw new ServiceException("Не выбрано подразделение");
	}
}
