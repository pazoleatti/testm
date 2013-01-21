package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.WrongInputDataServiceException;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Каноничный пример антипаттерна "Волшебный хандлер".
 * TODO: Разделить класс на 3: показ, создание формы, изменение статуса формы. Result можно оставить общий.
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
	FormTemplateService formTemplateService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException, WrongInputDataServiceException {
		checkAction(action);
		
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();

		FormData formData;
		if (action.getWorkFlowMove() != null) {
			formDataService.doMove(action.getFormDataId(), userId, action.getWorkFlowMove(), logger);
		}
		if(action.getFormDataId() == Long.MAX_VALUE){
			formData = formDataService.createFormData(logger, userId, formTemplateService.getActiveFormTemplateId(action.getFormDataTypeId().intValue()), action.getDepartmentId().intValue(),
					FormDataKind.fromId(action.getFormDataKind().intValue()));

			result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId().intValue()).getName());
			result.setDepartmenName(departmentDao.getDepartment(action.getDepartmentId()).getName());

			FormDataAccessParams accessParams = new FormDataAccessParams();
			accessParams.setCanDelete(false);
			accessParams.setCanEdit(true);
			accessParams.setCanRead(true);
			accessParams.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(0));
			result.setFormDataAccessParams(accessParams);
		} else{
			formData = formDataService.getFormData(userId, action.getFormDataId(), logger);
			result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
			result.setDepartmenName(departmentDao.getDepartment(formData.getDepartmentId()).getName());
			Long formDataId = formData.getId();
			FormDataAccessParams accessParams = accessService.getFormDataAccessParams(userId, formDataId);
			result.setFormDataAccessParams(accessParams);
		}
		result.setNumberedHeader(formTemplateService.get(formData.getFormTemplateId()).isNumberedColumns());
		result.setAllStyles(formTemplateService.get(formData.getFormTemplateId()).getStyles());
		result.setLogEntries(logger.getEntries());
		result.setFormData(formData);

		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
	
	private void checkAction(GetFormData action) throws WrongInputDataServiceException {
		String errorMessage = "";
		if (action.getFormDataId() == Long.MAX_VALUE) {
			if (action.getFormDataTypeId() == null) {
				errorMessage += "не указан вид формы";
			}
			if (action.getFormDataKind() == null) {
				errorMessage += ", не указан тип формы";
			}
			if (action.getDepartmentId() == null) {
				errorMessage += ", не указано подразделение";
			}
			if (!errorMessage.isEmpty()) {
				errorMessage =  errorMessage.startsWith(",") ? errorMessage.substring(1): errorMessage;
				throw new WrongInputDataServiceException(errorMessage);
			}
		}
	}
}
