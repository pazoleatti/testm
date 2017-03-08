package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class OpenPeriodHandler extends AbstractActionHandler<OpenPeriodAction, OpenPeriodResult> {

	@Autowired
	private PeriodService reportPeriodService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

	public OpenPeriodHandler() {
		super(OpenPeriodAction.class);
	}

	@Override
	public OpenPeriodResult execute(OpenPeriodAction action, ExecutionContext executionContext) throws ActionException {
		List<LogEntry> logs = new ArrayList<LogEntry>();
		reportPeriodService.open(action.getYear(), (int) action.getDictionaryTaxPeriodId(),
				action.getTaxType(), securityService.currentUserInfo(), action.getDepartmentId(), logs, action.isBalancePeriod(), action.getCorrectPeriod());
		OpenPeriodResult result = new OpenPeriodResult();
        result.setUuid(logEntryService.save(logs));
		return result;
	}

	@Override
	public void undo(OpenPeriodAction getPeriodDataAction, OpenPeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
