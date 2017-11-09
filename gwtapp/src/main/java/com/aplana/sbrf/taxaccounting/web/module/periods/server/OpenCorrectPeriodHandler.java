package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenCorrectPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenCorrectPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class OpenCorrectPeriodHandler extends AbstractActionHandler<OpenCorrectPeriodAction, OpenCorrectPeriodResult> {

    public OpenCorrectPeriodHandler() {
        super(OpenCorrectPeriodAction.class);
    }

    @Autowired
    private PeriodService periodService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;

    @Override
    public OpenCorrectPeriodResult execute(OpenCorrectPeriodAction action, ExecutionContext executionContext) throws ActionException {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        periodService.openCorrectionPeriod(action.getTaxType(), toReportPeriod(action.getSelectedPeriod()), action.getSelectedDepartments().get(0), new LocalDateTime(action.getTerm()), securityService.currentUserInfo(), logs);
        OpenCorrectPeriodResult result = new OpenCorrectPeriodResult();
        result.setUuid(logEntryService.save(logs));
        return result;
    }

    @Override
    public void undo(OpenCorrectPeriodAction openCorrectPeriodAction, OpenCorrectPeriodResult openCorrectPeriodResult, ExecutionContext executionContext) throws ActionException {

    }

    public ReportPeriod toReportPeriod(ReportPeriodViewModel selectedPeriod) {
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(selectedPeriod.getId());
        reportPeriod.setName(selectedPeriod.getName());
        reportPeriod.setAccName(selectedPeriod.getAccName());
        reportPeriod.setOrder(selectedPeriod.getOrder());
        reportPeriod.setTaxPeriod(selectedPeriod.getTaxPeriod());
        reportPeriod.setStartDate(new LocalDateTime(selectedPeriod.getStartDate()));
        reportPeriod.setEndDate(new LocalDateTime(selectedPeriod.getEndDate()));
        reportPeriod.setCalendarStartDate(new LocalDateTime(selectedPeriod.getCalendarStartDate()));
        reportPeriod.setDictTaxPeriodId(selectedPeriod.getDictTaxPeriodId());
        return reportPeriod;
    }
}
