package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.RemovePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.RemovePeriodResult;
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
public class RemovePeriodHandler extends AbstractActionHandler<RemovePeriodAction, RemovePeriodResult> {

	public RemovePeriodHandler() {
		super(RemovePeriodAction.class);
	}

	@Autowired
	PeriodService periodService;
	@Autowired
	SecurityService securityService;
	@Autowired
	LogEntryService logEntryService;

	@Override
	public RemovePeriodResult execute(RemovePeriodAction removePeriodAction, ExecutionContext executionContext) throws ActionException {
		List<LogEntry> logs = new ArrayList<LogEntry>();
		periodService.removeReportPeriod(removePeriodAction.getTaxType(), removePeriodAction.getReportPeriodId(),
				removePeriodAction.getDepartmentId(), logs, securityService.currentUserInfo());
		RemovePeriodResult result = new RemovePeriodResult();
		result.setUuid(logEntryService.save(logs));
		return result;
	}

	@Override
	public void undo(RemovePeriodAction removePeriodAction, RemovePeriodResult removePeriodResult, ExecutionContext executionContext) throws ActionException {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
