package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasNotAcceptedFormAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasNotAcceptedFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CheckHasNotAcceptedFormHandler extends AbstractActionHandler<CheckHasNotAcceptedFormAction, CheckHasNotAcceptedFormResult> {

    public CheckHasNotAcceptedFormHandler() {
        super(CheckHasNotAcceptedFormAction.class);
    }

    @Autowired
    private DeclarationDataSearchService declarationDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private FormDataSearchService formDataSearchService;

    @Override
    public CheckHasNotAcceptedFormResult execute(CheckHasNotAcceptedFormAction action, ExecutionContext executionContext) throws ActionException {
        List<LogEntry> logs = new ArrayList<LogEntry>();

        List<Integer> departments = departmentService.getAllChildrenIds(action.getDepartmentId());
        FormDataFilter dataFilter = new FormDataFilter();
        dataFilter.setDepartmentIds(departments);
        dataFilter.setReportPeriodIds(Arrays.asList(action.getReportPeriodId()));
        if (action.getCorrectPeriod() != null) {
            dataFilter.setCorrectionTag(true);
            dataFilter.setCorrectionDate(action.getCorrectPeriod());
        } else {
            dataFilter.setCorrectionTag(false);
        }
        List<FormData> forms = formDataSearchService.findDataByFilter(dataFilter);

        for (FormData fd : forms) {
            if (fd.getState() != WorkflowState.ACCEPTED) {
                logs.add(new LogEntry(LogLevel.WARNING, "Форма \"" + fd.getFormType().getName() + "\" \"" + fd.getKind().getName() +
                    "\" в подразделении \"" + departmentService.getDepartment(fd.getDepartmentId()).getName() +
                    "\"  находится в состоянии отличном от \"Принята\"" )
                );
            }
        }

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departments);
        filter.setReportPeriodIds(Arrays.asList(action.getReportPeriodId()));
        filter.setFormState(WorkflowState.CREATED);
        if (action.getCorrectPeriod() != null) {
            filter.setCorrectionTag(true);
            filter.setCorrectionDate(action.getCorrectPeriod());
        } else {
            filter.setCorrectionTag(false);
        }

        List<DeclarationData> declarations = declarationDataService.getDeclarationData(filter, DeclarationDataSearchOrdering.ID, false);

        for (DeclarationData dd : declarations) {
            StringBuilder msg = new StringBuilder();
            msg.append("Для декларации/уведомления: ");
            msg.append("\"" + declarationTemplateService.get(dd.getDeclarationTemplateId()).getType().getName() + "\"");
            DeclarationType declarationType = declarationTemplateService.get(dd.getDeclarationTemplateId()).getType();
            if (declarationType.getTaxType() == TaxType.PROPERTY || TaxType.TRANSPORT.equals(declarationType.getTaxType())) {
                msg.append(" (налоговый орган \"" + dd.getTaxOrganCode() + "\", КПП \"" + dd.getKpp() + "\")");
            }
            msg.append(" в подразделении ");
            msg.append("\"" + departmentService.getDepartment(dd.getDepartmentId()).getName() + "\"");
            msg.append(" находится в состоянии отличном от \"Принята\"");

            logs.add(new LogEntry(LogLevel.WARNING, msg.toString()));
        }

        CheckHasNotAcceptedFormResult result = new CheckHasNotAcceptedFormResult();
        result.setHasNotAcceptedForms(!logs.isEmpty());
        result.setUuid(logEntryService.save(logs));

        return result;
    }

    @Override
    public void undo(CheckHasNotAcceptedFormAction checkHasNotAcceptedFormAction, CheckHasNotAcceptedFormResult checkHasNotAcceptedFormResult, ExecutionContext executionContext) throws ActionException {

    }
}
