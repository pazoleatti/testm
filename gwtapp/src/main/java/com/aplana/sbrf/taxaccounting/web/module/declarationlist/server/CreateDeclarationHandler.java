package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclaration;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclarationResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateDeclarationHandler extends AbstractActionHandler<CreateDeclaration, CreateDeclarationResult> {

	public CreateDeclarationHandler() {
		super(CreateDeclaration.class);
	}

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

	@Autowired
    private DeclarationDataService declarationDataService;

	@Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PeriodService reportPeriodService;

	@Override
	public CreateDeclarationResult execute(CreateDeclaration command, ExecutionContext executionContext) throws ActionException {

        CreateDeclarationResult result = new CreateDeclarationResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        String key = LockData.LockObjects.DECLARATION_CREATE.name() + "_" + command.getDeclarationTypeId() + "_" + command.getTaxType().getName() + "_" + command.getDepartmentId() + "_" + command.getReportPeriodId() + "_" + command.getTaxOrganKpp() + "_" + command.getTaxOrganCode();

        DeclarationType declarationType = declarationTypeService.get(command.getDeclarationTypeId());
        Department department = departmentService.getDepartment(command.getDepartmentId());
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(command.getReportPeriodId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(command.getDepartmentId(),
                command.getReportPeriodId());
        Integer declarationTypeId = command.getDeclarationTypeId();

        if (departmentReportPeriod == null) {
            throw new ActionException("Не удалось определить налоговый период.");
        }

        if (lockDataService.lock(key, userInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        String.format("Создание %s", declarationType.getTaxType().getDeclarationShortName()),
                        reportPeriod.getName() + " " + reportPeriod.getTaxPeriod().getYear(),
                        departmentReportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(departmentReportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationType.getName(),
                        command.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + command.getTaxOrganCode() + "\""
                                : "",
                        command.getTaxOrganKpp() != null
                                ? ", КПП: \"" + command.getTaxOrganKpp() + "\""
                                : "")) == null) {
            //Если блокировка успешно установлена
            try {

                DeclarationData declarationData = declarationDataService.find(declarationTypeId, departmentReportPeriod.getId(), command.getTaxOrganKpp(), command.getTaxOrganCode(), null, null);
                if (declarationData != null) {
                    String msg = (declarationType.getTaxType().equals(TaxType.DEAL) ?
                            "Уведомление с заданными параметрами уже существует" :
                            "Декларация с заданными параметрами уже существует");
                    logger.error(msg);
                    result.setDeclarationId(null);
                    lockDataService.unlock(key, userInfo.getUser().getId());
                    return result;
                }

                int activeDeclarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId,
                        departmentReportPeriod.getReportPeriod().getId());

                long declarationId = declarationDataService.create(logger, activeDeclarationTemplateId,
                        securityService.currentUserInfo(), departmentReportPeriod, command.getTaxOrganCode(),
                        command.getTaxOrganKpp(), null, null);

                result.setDeclarationId(declarationId);

                lockDataService.unlock(key, userInfo.getUser().getId());
                return result;
            } catch (Exception e) {
                try {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                } catch (ServiceException e2) {
                    if (PropertyLoader.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутствии блокировки, если она снята при выбрасывании исключения
                        throw new ActionException(e2);
                    }
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ActionException(e);
                }
            } finally {
                if (!logger.getEntries().isEmpty()){
                    result.setUuid(logEntryService.save(logger.getEntries()));
                }
            }
        } else {
            throw new ActionException("Создание декларации с указанными параметрами уже выполняется!");
        }
	}

	@Override
	public void undo(CreateDeclaration createDeclaration, CreateDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
