package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
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
        Logger logger = new Logger();
		periodService.removeReportPeriod(removePeriodAction.getTaxType(), removePeriodAction.getDepartmentReportPeriodId(), logger, securityService.currentUserInfo());
		RemovePeriodResult result = new RemovePeriodResult();
        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setHasFatalErrors(true);
        }
		result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(RemovePeriodAction removePeriodAction, RemovePeriodResult removePeriodResult, ExecutionContext executionContext) throws ActionException {

	}
}
