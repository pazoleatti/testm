package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class ClosePeriodHandler extends AbstractActionHandler<ClosePeriodAction, ClosePeriodResult> {

	public ClosePeriodHandler() {
		super(ClosePeriodAction.class);
	}

	@Autowired
	private PeriodService reportPeriodService;

    @Autowired
    private LogEntryService logEntryService;

	@Override
	public ClosePeriodResult execute(ClosePeriodAction action, ExecutionContext executionContext) throws ActionException {
		List<LogEntry> logs = new ArrayList<LogEntry>();
		reportPeriodService.close(action.getTaxType(), action.getReportPeriodId(), action.getDepartmentId(), logs);
		ClosePeriodResult result = new ClosePeriodResult();
        result.setUuid(logEntryService.save(logs));
		return result;
	}

	@Override
	public void undo(ClosePeriodAction closePeriodAction, ClosePeriodResult closePeriodResult, ExecutionContext executionContext) throws ActionException {
	}
}
