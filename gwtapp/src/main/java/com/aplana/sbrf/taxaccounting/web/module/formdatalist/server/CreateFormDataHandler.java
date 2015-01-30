package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateFormDataHandler extends AbstractActionHandler<CreateFormData, CreateFormDataResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private FormTypeService formTypeService;

	@Autowired
	FormTemplateService formTemplateService;

    @Autowired
    SourceService sourceService;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    private static final String ERROR_SELECT_REPORT_PERIOD = "Период подразделения не выбран!";
    private static final String ERROR_SELECT_DEPARTMENT = "Подразделение не выбрано!";
    private static final String ERROR_SELECT_FORM_DATA_KIND = "Тип налоговой формы не выбран!";
    private static final String ERROR_SELECT_FORM_DATA_TYPE = "Вид налоговой формы не выбран!";
    private static final String ERROR_DEPARTMENT_REPORT_PERIOD_NOT_FOUND = "Не определен отчетный период подразделения!";
    private final static String MANUAL_USED_MESSAGE = "Для формирования декларации в корректируемом периоде используются данные версии ручного ввода, созданной в форме «%s», %s, «%s»!";

    public CreateFormDataHandler() {
		super(CreateFormData.class);
	}

	@Override
	public CreateFormDataResult execute(CreateFormData action, ExecutionContext context) throws ActionException {

		TAUserInfo userInfo = securityService.currentUserInfo();
		checkAction(action);
		CreateFormDataResult result = new CreateFormDataResult();
		Logger logger = new Logger();
        String key = LockData.LockObjects.FORM_DATA_CREATE.name() + "_" + action.getFormDataTypeId() + "_" + action.getFormDataKindId() + "_" + action.getDepartmentId() + "_" + action.getReportPeriodId() + "_" + action.getMonthId();

        if (lockDataService.lock(key, userInfo.getUser().getId(),
                lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA_CREATE)) == null) {
            //Если блокировка успешно установлена
            try {
                // Подставляется последний отчетный период подразделения
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(action.getDepartmentId(),
                        action.getReportPeriodId());

                if (departmentReportPeriod == null) {
                    throw new ServiceException(ERROR_DEPARTMENT_REPORT_PERIOD_NOT_FOUND);
                }

                /**
                 *  Проверка существования назначений источников-приемников
                 */

                // 1. Если форма является приемником данных в указанном периоде, то Система выводит сообщение в панель уведомления предупреждение: "Форма является приемником данных."
                FormDataKind kind = FormDataKind.fromId(action.getFormDataKindId());
                Integer formDataTypeId = action.getFormDataTypeId();
                List<DepartmentFormType> sources = sourceService.getDFTSourcesByDFT(departmentReportPeriod.getDepartmentId(),
                        formDataTypeId, kind, departmentReportPeriod.getReportPeriod().getId());
                if (!sources.isEmpty()){
                    logger.warn("Форма является приемником данных.");
                }

                // 2. Если форма не является источников данных для других налоговых форм и деклараций в указанном периоде, то Система выводит сообщение в панель уведомления предупреждение: "Не найдены назначения источников-приемников в периоде <Период создания формы>. Форма не является источником данных для декларации."
                List<DepartmentDeclarationType> declarationDestinations = sourceService.getDeclarationDestinations(
                        departmentReportPeriod.getDepartmentId(), formDataTypeId, kind, departmentReportPeriod.getReportPeriod().getId());
                List<DepartmentFormType> formDestinations = sourceService.getFormDestinations(
                        departmentReportPeriod.getDepartmentId(), formDataTypeId, kind, departmentReportPeriod.getReportPeriod().getId());
                if (declarationDestinations.isEmpty() && formDestinations.isEmpty()){
                    logger.warn("Не найдены назначения источников-приемников в периоде " + departmentReportPeriod.getReportPeriod().getName() + ". Форма не является источником данных для декларации.");
                }

                int templateId = formTemplateService.getActiveFormTemplateId(formDataTypeId, departmentReportPeriod.getReportPeriod().getId());
                long formDataId = formDataService.createFormData(logger, userInfo, templateId, departmentReportPeriod.getId(),
                        kind, action.getMonthId());

                // Если декларация является приемником и есть форма ручного ввода в корректируемом периоде
                List<FormData> manualInputForms = formDataService.getManualInputForms(
                        Arrays.asList(departmentReportPeriod.getDepartmentId()),
                        departmentReportPeriod.getReportPeriod().getId(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getTaxType(), kind);
                if (departmentReportPeriod.getCorrectionDate() != null && !declarationDestinations.isEmpty() && !manualInputForms.isEmpty()) {
                    Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                    FormType formType = formTypeService.get(formDataTypeId);
                    logger.info(String.format(MANUAL_USED_MESSAGE, formType.getName(), kind.getName(), department.getName()));
                }

                result.setFormDataId(formDataId);

                lockDataService.unlock(key, userInfo.getUser().getId());
                return result;
            } catch (Exception e) {
                try {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                } catch (ServiceException e2) {
                    if (PropertyLoader.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутсвии блокировки, если оня снята при выбрасывании исключения
                        throw new ActionException(e2);
                    }
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ActionException(e);
                }
            } finally {
                if (logger.getEntries().size() != 0){
                    result.setUuid(logEntryService.save(logger.getEntries()));
                }
            }
        } else {
            throw new ActionException("Создание формы с указанными параметрами уже выполняется!");
        }
	}

	private void checkAction(CreateFormData action) throws ActionException {
        // Проверки заполнения полей (частичное дублирование клиентского кода)
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
