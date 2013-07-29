package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ChangeActivePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ChangeActivePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangeActivePeriodHandler extends AbstractActionHandler<ChangeActivePeriodAction, ChangeActivePeriodResult> {

	@Autowired
	private ReportPeriodService reportPeriodService;

	public ChangeActivePeriodHandler() {
		super(ChangeActivePeriodAction.class);
	}

	@Override
	public ChangeActivePeriodResult execute(ChangeActivePeriodAction action, ExecutionContext executionContext) throws ActionException {
		ChangeActivePeriodResult result = new ChangeActivePeriodResult();
		if (action.isActive()) {
			reportPeriodService.openPeriod(action.getReportPeriodId());
		} else {
			reportPeriodService.closePeriod(action.getReportPeriodId());
		}
		return result;
	}

	@Override
	public void undo(ChangeActivePeriodAction getPeriodDataAction, ChangeActivePeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
