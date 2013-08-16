package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult.FormMode;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFormDataHandler extends
		AbstractActionHandler<GetFormData, GetFormDataResult> {

	@Autowired
	private FormDataAccessService accessService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private ReportPeriodService reportPeriodService;

	@Autowired
	private TaxPeriodService taxPeriodService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	FormTemplateService formTemplateService;

    @Autowired
    BlobDataService blobDataService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}

	@Override
	public GetFormDataResult execute(GetFormData action,
			ExecutionContext context) throws ActionException {

		TAUserInfo userInfo = securityService.currentUserInfo();

		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();

		fillLockData(action, userInfo, result);
		fillFormAndTemplateData(action, userInfo, logger, result);
		fillFormDataAccessParams(action, userInfo, result);

		result.setLogEntries(logger.getEntries());

		return result;

	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}

	/**
	 * Получает/создает данные налоговой формы
	 * 
	 * @param action
	 * @param userInfo
	 * @param logger
	 * @param result
	 */
	private void fillFormAndTemplateData(GetFormData action, TAUserInfo userInfo,
			Logger logger, GetFormDataResult result) {
		FormData formData;
		
		if (!action.isReadOnly()) {
			formDataService.lock(action.getFormDataId(), userInfo);
		}
		
		formData = formDataService.getFormData(userInfo,
				action.getFormDataId(), logger);
		
		FormTemplate formTemplate = formTemplateService.get(formData
				.getFormTemplateId());

		ReportPeriod reportPeriod = reportPeriodService.get(formData.getReportPeriodId());
		result.setReportPeriod(reportPeriod);
		result.setDepartmenName(departmentService.getDepartment(
				formData.getDepartmentId()).getName());
		result.setNumberedHeader(formTemplate.isNumberedColumns());
		result.setAllStyles(formTemplate.getStyles());
		result.setFixedRows(formTemplate.isFixedRows());
		result.setTemplateFormName(formTemplate.getName());
		result.setFormData(formData);

		TaxPeriod taxPeriod = taxPeriodService.get(reportPeriod.getTaxPeriodId());
		result.setTaxPeriodStartDate(taxPeriod.getStartDate());
		result.setTaxPeriodEndDate(taxPeriod.getEndDate());
	}

	/**
	 * Заполняет параметры доступа для формы
	 * 
	 * @param action
	 * @param userInfo
	 * @param result
	 */
	private void fillFormDataAccessParams(GetFormData action, TAUserInfo userInfo,
			GetFormDataResult result) {
		FormDataAccessParams accessParams;
		if (action.getFormDataId() == Long.MAX_VALUE) {
			accessParams = new FormDataAccessParams();
			accessParams.setCanDelete(false);
			accessParams.setCanEdit(true);
			accessParams.setCanRead(true);
			accessParams.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(
					0));
		} else {
			accessParams = accessService.getFormDataAccessParams(userInfo, result
					.getFormData().getId());
		}
		result.setFormDataAccessParams(accessParams);
	}

	/**
	 * Блокирует форму при необходимости, заполняет состояние блокировки
	 * 
	 * @param action
	 * @param userInfo
	 * @param result
	 */
	private void fillLockData(GetFormData action, TAUserInfo userInfo,
			GetFormDataResult result) {
		FormMode formMode = FormMode.READ_LOCKED;

		ObjectLock<Long> lockInformation = formDataService.getObjectLock(action
				.getFormDataId());
		if (lockInformation != null) {
			// Если данная форма уже заблокирована другим пользотелем

			result.setLockedByUser(userInfo.getUser().getName());
			result.setLockDate(getFormedDate(lockInformation.getLockTime()));
			if (lockInformation.getUserId() == userInfo.getUser().getId()) {
				if (action.isReadOnly()) {
					formMode = FormMode.READ_UNLOCKED;
				} else {
					formMode = FormMode.EDIT;
				}
			}
		} else {

				if (action.isReadOnly()) {
					formMode = FormMode.READ_UNLOCKED;
				} else {
					formMode = FormMode.EDIT;
				}
			
		}
		result.setFormMode(formMode);
	}

	private static String getFormedDate(Date dateToForm) {
		// Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		formatter.format(dateToForm);
		return (formatter.format(dateToForm));
	}
}
