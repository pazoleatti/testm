package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasNotAcceptedFormAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasNotAcceptedFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
public class CheckHasNotAcceptedFormHandler extends AbstractActionHandler<CheckHasNotAcceptedFormAction, CheckHasNotAcceptedFormResult> {

    private static final String FD_NOT_ACCEPTED = "Форма находится в состоянии отличном от \"Принята\": ";

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
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Override
    public CheckHasNotAcceptedFormResult execute(CheckHasNotAcceptedFormAction action, ExecutionContext executionContext) throws ActionException {
        Logger logger = new Logger();

        List<Integer> departments = departmentService.getAllChildrenIds(action.getDepartmentId());

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departments);
        filter.setReportPeriodIds(Arrays.asList(action.getReportPeriodId()));
        filter.setFormState(State.CREATED);
        if (action.getCorrectPeriod() != null) {
            filter.setCorrectionTag(true);
            filter.setCorrectionDate(action.getCorrectPeriod());
        } else {
            filter.setCorrectionTag(false);
        }

        List<DeclarationData> declarations = declarationDataService.getDeclarationData(filter, DeclarationDataSearchOrdering.ID, false);
        filter.setFormState(State.PREPARED);
        declarations.addAll(declarationDataService.getDeclarationData(filter, DeclarationDataSearchOrdering.ID, false));
        for (DeclarationData dd : declarations) {
            StringBuilder msg = new StringBuilder();
            msg.append("Налоговая форма: №: ");
            msg.append(dd.getId()).append(", Вид: ");
            msg.append("\"").append(declarationTemplateService.get(dd.getDeclarationTemplateId()).getType().getName()).append("\"");
            DeclarationType declarationType = declarationTemplateService.get(dd.getDeclarationTemplateId()).getType();
            /*if (declarationType.getTaxType() == TaxType.PROPERTY || TaxType.TRANSPORT.equals(declarationType.getTaxType())) {
                msg.append(" (налоговый орган \"").append(dd.getTaxOrganCode()).append("\", КПП \"").append(dd.getKpp()).append("\")");
            }*/
            msg.append(", Подразделение: ");
            msg.append("\"").append(departmentService.getDepartment(dd.getDepartmentId()).getName()).append("\"");
            msg.append(", находится в состоянии отличном от \"Принята\"");

            logger.warn(msg.toString());
        }

        CheckHasNotAcceptedFormResult result = new CheckHasNotAcceptedFormResult();
        result.setHasNotAcceptedForms(!logger.getEntries().isEmpty());
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(CheckHasNotAcceptedFormAction checkHasNotAcceptedFormAction, CheckHasNotAcceptedFormResult checkHasNotAcceptedFormResult, ExecutionContext executionContext) throws ActionException {

    }
}
