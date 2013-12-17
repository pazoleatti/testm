package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class OpenPeriodHandler extends AbstractActionHandler<OpenPeriodAction, OpenPeriodResult> {

	@Autowired
	private PeriodService reportPeriodService;

	@Autowired
	private SecurityService securityService;

	public OpenPeriodHandler() {
		super(OpenPeriodAction.class);
	}

	@Override
	public OpenPeriodResult execute(OpenPeriodAction action, ExecutionContext executionContext) throws ActionException {
		List<LogEntry> logs = new ArrayList<LogEntry>();
		reportPeriodService.open(action.getYear(), (int) action.getDictionaryTaxPeriodId(),
				action.getTaxType(), securityService.currentUserInfo(), action.getDepartmentId(), logs, action.isBalancePeriod(), action.getCorrectPeriod(), action.isHasCorrectPeriod());
		OpenPeriodResult result = new OpenPeriodResult();
		result.setLogEntries(logs);
		return result;
	}

	@Override
	public void undo(OpenPeriodAction getPeriodDataAction, OpenPeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
