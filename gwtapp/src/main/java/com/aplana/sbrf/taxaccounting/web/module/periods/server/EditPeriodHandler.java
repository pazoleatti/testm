package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.EditPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.EditPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class EditPeriodHandler extends AbstractActionHandler<EditPeriodAction, EditPeriodResult> {

    public EditPeriodHandler() {
        super(EditPeriodAction.class);
    }

    @Autowired
    private PeriodService periodService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;

    @Override
    public EditPeriodResult execute(EditPeriodAction action, ExecutionContext executionContext) throws ActionException {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        if (action.getCorrectionDate() == null) {
            periodService.edit(action.getReportPeriodId(), action.getOldDepartmentId(), action.getNewDictTaxPeriodId(), action.getYear(),
                    action.getTaxType(), securityService.currentUserInfo(), action.getNewDepartmentId(), action.isBalance(), logs);
        } else {
            periodService.editCorrectionPeriod(action.getReportPeriodId(), action.getNewReportPeriodId(), action.getOldDepartmentId(),
                    action.getNewDepartmentId(), action.getTaxType(), action.getCorrectionDate(), action.getNewCorrectionDate(),  securityService.currentUserInfo(), logs);
        }
        EditPeriodResult result = new EditPeriodResult();
        result.setUuid(logEntryService.save(logs));
        return result;
    }

    @Override
    public void undo(EditPeriodAction editPeriodAction, EditPeriodResult editPeriodResult, ExecutionContext executionContext) throws ActionException {
    }
}
