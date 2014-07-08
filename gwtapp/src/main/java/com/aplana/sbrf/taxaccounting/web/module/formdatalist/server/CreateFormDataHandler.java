package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.util.List;

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

    @Autowired
    SourceService sourceService;

    @Autowired
    LogEntryService logEntryService;

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
        Integer reportPeriodId = action.getReportPeriodId();
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);

        /**
         *  Проверка существования назначений источников-приемников
         */

        // 1. Если форма является приемником данных в указанном периоде, то Система выводит сообщение в панель уведомления предупреждение: "Форма является приемником данных."
        Integer formDataKindId = action.getFormDataKindId();
        FormDataKind kind = FormDataKind.fromId(formDataKindId);
        Integer departmentId = action.getDepartmentId();
        Integer formDataTypeId = action.getFormDataTypeId();
        List<DepartmentFormType> sources = sourceService.getDFTSourcesByDFT(departmentId, formDataTypeId, kind, reportPeriodId);
        if (!sources.isEmpty()){
            logger.warn("Форма является приемником данных.");
        }

        // 2. Если форма не является источников данных для других налоговых форм и деклараций в указанном периоде, то Система выводит сообщение в панель уведомления предупреждение: "Не найдены назначения источников-приемников в периоде <Период создания формы>. Форма не является источником данных для декларации."
        List<DepartmentDeclarationType> declarationDestinations = sourceService.getDeclarationDestinations(departmentId, formDataTypeId, kind, reportPeriodId);
        List<DepartmentFormType> formDestinations = sourceService.getFormDestinations(departmentId, formDataTypeId, kind, reportPeriodId);
        if (declarationDestinations.isEmpty() && formDestinations.isEmpty()){
            logger.warn("Не найдены назначения источников-приемников в периоде " + reportPeriod.getName() + ". Форма не является источником данных для декларации.");
        }

        // TODO Левыкин: для ежемесячных форм передавать periodOrder
        result.setFormDataId(formDataService.createFormData(logger, userInfo,
				formTemplateService.getActiveFormTemplateId(formDataTypeId, reportPeriodId), departmentId, FormDataKind.fromId(formDataKindId),
                        reportPeriod,
                        action.getMonthId() != null ? action.getMonthId() : null));

        if (logger.getEntries().size() != 0){
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

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
