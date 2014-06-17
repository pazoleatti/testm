package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateFormDataHandler extends AbstractActionHandler<CreateFormData, CreateFormDataResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private PeriodService reportPeriodService;

	@Autowired
	FormTemplateService formTemplateService;

    private static final String ERROR_SELECT_REPORT_PERIOD = "Период не выбран!";
    private static final String ERROR_SELECT_DEPARTMENT = "Подразделение не выбрано!";
    private static final String ERROR_SELECT_FORM_DATA_KIND = "Тип налоговой формы не выбран!";
    private static final String ERROR_SELECT_FORM_DATA_TYPE = "Вид налоговой формы не выбран!";

    public CreateFormDataHandler() {
		super(CreateFormData.class);
	}

	@Override
	public CreateFormDataResult execute(CreateFormData action, ExecutionContext context) throws ActionException {

		TAUserInfo userInfo = securityService.currentUserInfo();
		checkAction(action);
		CreateFormDataResult result = new CreateFormDataResult();
		Logger logger = new Logger();

		result.setFormDataId(formDataService.createFormData(logger, userInfo,
				formTemplateService.getActiveFormTemplateId(action
						.getFormDataTypeId(), action.getReportPeriodId()), action
						.getDepartmentId(), FormDataKind.fromId(action
						.getFormDataKindId()),
						reportPeriodService.getReportPeriod(action.getReportPeriodId()),
                        action.getMonthId() != null ? action.getMonthId() : null));

		return result;
	}

	private void checkAction(CreateFormData action) throws ActionException {
        // Проверки заполнения полей (частичное дублирование клиентского кода)
        if (action.getReportPeriodId() == null) {
            throw new TaActionException(ERROR_SELECT_REPORT_PERIOD);
		}
        if (action.getDepartmentId() == null) {
            throw new TaActionException(ERROR_SELECT_DEPARTMENT);
        }
        if (action.getFormDataKindId() == null) {
            throw new TaActionException(ERROR_SELECT_FORM_DATA_KIND);
        }
        if (action.getFormDataTypeId() == null) {
            throw new TaActionException(ERROR_SELECT_FORM_DATA_TYPE);
        }
        // Остальные проверки реализованы в FormDataAccessService#canCreate и вызываются перед созданием формы
	}

	@Override
	public void undo(CreateFormData action, CreateFormDataResult result,
			ExecutionContext context) throws ActionException {
	}
}
