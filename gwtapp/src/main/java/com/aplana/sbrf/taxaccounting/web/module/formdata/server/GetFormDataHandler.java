package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
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
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFormDataHandler extends
		AbstractActionHandler<GetFormData, GetFormDataResult> {

	@Autowired
	private FormDataAccessService accessService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private PeriodService reportPeriodService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private FormTemplateService formTemplateService;

    @Autowired 
    private TAUserService taUserService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookFactory refBookFactory;

    private static final long REF_BOOK_ID = 8L;
    private static final String REF_BOOK_VALUE_NAME = "CODE";

	public GetFormDataHandler() {
		super(GetFormData.class);
	}

	@Override
	public GetFormDataResult execute(GetFormData action,
			ExecutionContext context) throws ActionException {

		TAUserInfo userInfo = securityService.currentUserInfo();
		
		// UNLOCK: Попытка разблокировать ту форму которая была открыта ранее
		try {
			if (action.getOldFormDataId() != null) {
				formDataService.unlock(action.getOldFormDataId(), userInfo);
			}
		} catch (Exception e){
			//
		}

		// LOCK: Попытка заблокировать форму которую хотим получить для редактирования
		if (!action.isReadOnly()) {
			try {
				formDataService.lock(action.getFormDataId(), userInfo);
			} catch (Exception e){
				//
			}
		}

		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();

		fillLockData(action, userInfo, result);
		fillFormAndTemplateData(action, userInfo, logger, result);
		fillFormDataAccessParams(action, userInfo, result);
        result.setUuid(logEntryService.update(logger.getEntries(), action.getUuid()));

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

		FormData formData = formDataService.getFormData(userInfo, action.getFormDataId(), action.isManual(), logger);

		FormTemplate formTemplate = formTemplateService.getFullFormTemplate(formData.getFormTemplateId());

		ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
        // http://jira.aplana.com/browse/SBRFACCTAX-6399
        if (formData.getKind() == FormDataKind.PRIMARY && reportPeriod.getTaxPeriod().getTaxType() == TaxType.INCOME) {
            RefBookDataProvider dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID);
            Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData((long) reportPeriod.getDictTaxPeriodId());
            Integer code = Integer.parseInt(refBookValueMap.get(REF_BOOK_VALUE_NAME).getStringValue());
            reportPeriod.setName(ReportPeriodSpecificNave.fromId(code).getName());
        }
        result.setBalancePeriod(reportPeriodService.isBalancePeriod(formData.getReportPeriodId(), formData.getDepartmentId()));
		result.setReportPeriod(reportPeriod);
		result.setDepartmenName(departmentService.getDepartment(
				formData.getDepartmentId()).getName());
        result.setDepartmenFullName(departmentService.getParentsHierarchy(
                formData.getDepartmentId()));
		result.setAllStyles(formTemplate.getStyles());
		result.setFixedRows(formTemplate.isFixedRows());
		result.setTemplateFormName(formTemplate.getName());
		result.setFormData(formData);
		result.setReportPeriodStartDate(reportPeriod.getCalendarStartDate());
		result.setReportPeriodEndDate(reportPeriod.getEndDate());

		result.setFormInClosedPeriod(!reportPeriodService.isActivePeriod(result.getReportPeriod().getId(), formData.getDepartmentId()));

        result.setReportPeriodYear(reportPeriod.getTaxPeriod().getYear());
        boolean isBankSummaryForm = formDataService.isBankSummaryForm(action.getFormDataId());
        result.setBankSummaryForm(isBankSummaryForm);
        if (isBankSummaryForm) {
            result.setExistManual(formDataService.existManual(action.getFormDataId()));
        } else {
            //Если это не сводная банка, то нет смысла проверять наличие версии ручного ввода
            result.setExistManual(false);
        }

        //Является ли форма последней перед декларацией
        List<DepartmentDeclarationType> declarationDestinations = sourceService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind());
        result.setCanCreatedManual((formData.getKind().equals(FormDataKind.CONSOLIDATED) || formData.getKind().equals(FormDataKind.SUMMARY)) && !declarationDestinations.isEmpty());
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
					.getFormData().getId(),
                    result.getFormData().isManual());
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
				.getFormDataId(), securityService.currentUserInfo());
		if (lockInformation != null) {
			
			// Если данная форма уже заблокирована другим пользотелем
			result.setLockedByUser(taUserService.getUser(lockInformation.getUserId()).getName());
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
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
		formatter.format(dateToForm);
		return (formatter.format(dateToForm));
	}
}
