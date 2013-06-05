package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult.FormMode;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

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
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	FormTemplateService formTemplateService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}

	@Override
	public GetFormDataResult execute(GetFormData action,
			ExecutionContext context) throws ActionException {

		TAUserInfo userInfo = securityService.currentUserInfo();
		checkAction(action);
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
		if (action.getFormDataId() == Long.MAX_VALUE) {
			formData = formDataService.createFormData(logger, userInfo,
					formTemplateService.getActiveFormTemplateId(action
							.getFormDataTypeId().intValue()), action
							.getDepartmentId(), FormDataKind.fromId(action
							.getFormDataKind().intValue()),
							reportPeriodDao.get(action.getReportPeriodId().intValue()));
		} else {
			if (!action.isReadOnly()) {
				formDataService.lock(action.getFormDataId(), userInfo);
			}
			formData = formDataService.getFormData(userInfo,
					action.getFormDataId(), logger);
		}
		FormTemplate formTemplate = formTemplateService.get(formData
				.getFormTemplateId());

		result.setReportPeriod(reportPeriodDao
				.get(formData.getReportPeriodId()));
		result.setDepartmenName(departmentService.getDepartment(
				formData.getDepartmentId()).getName());
		result.setNumberedHeader(formTemplate.isNumberedColumns());
		result.setAllStyles(formTemplate.getStyles());
		result.setFixedRows(formTemplate.isFixedRows());
		result.setTemplateFormName(formTemplate.getName());
		result.setFormData(formData);
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

	private void checkAction(GetFormData action) throws ActionException {
		String errorMessage = "";
		if (action.getFormDataId() == Long.MAX_VALUE) {
			if (action.getFormDataTypeId() == null) {
				errorMessage += " не указан вид формы";
			}
			if (action.getFormDataKind() == null) {
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
			// Если данная форма никем не заблокирована или это новая форма
			if (action.getFormDataId() == Long.MAX_VALUE) {
				formMode = FormMode.EDIT;
			} else {
				if (action.isReadOnly()) {
					formMode = FormMode.READ_UNLOCKED;
				} else {
					formMode = FormMode.EDIT;
				}
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
