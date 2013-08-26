package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class CreateFormDataHandler extends
		AbstractActionHandler<CreateFormData, CreateFormDataResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private ReportPeriodService reportPeriodService;

	@Autowired
	FormTemplateService formTemplateService;

	public CreateFormDataHandler() {
		super(CreateFormData.class);
	}

	@Override
	public CreateFormDataResult execute(CreateFormData action,
			ExecutionContext context) throws ActionException {

		TAUserInfo userInfo = securityService.currentUserInfo();
		checkAction(action);
		CreateFormDataResult result = new CreateFormDataResult();
		Logger logger = new Logger();

		result.setFormDataId(formDataService.createFormData(logger, userInfo,
				formTemplateService.getActiveFormTemplateId(action
						.getFormDataTypeId().intValue()), action
						.getDepartmentId(), FormDataKind.fromId(action
						.getFormDataKindId().intValue()),
						reportPeriodService.get(action.getReportPeriodId().intValue())));

		return result;

	}


	private void checkAction(CreateFormData action) throws ActionException {
		String errorMessage = "";

			if (action.getFormDataTypeId() == null) {
				errorMessage += " не указан вид формы";
			}
			if (action.getFormDataKindId() == null) {
				errorMessage += ", не указан тип формы";
			}
			if (action.getDepartmentId() == null) {
				errorMessage += ", не указано подразделение";
			}
			if (action.getReportPeriodId() == null) {
				errorMessage += ", не указан отчетный период";
			}
			if (!errorMessage.isEmpty()) {
				errorMessage = errorMessage.startsWith(",") ? errorMessage
						.substring(1) : errorMessage;
				throw new TaActionException(
						"Не удалось создать налоговую форму:" + errorMessage);
			}
		
	}

	@Override
	public void undo(CreateFormData action, CreateFormDataResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
		
	}
}
