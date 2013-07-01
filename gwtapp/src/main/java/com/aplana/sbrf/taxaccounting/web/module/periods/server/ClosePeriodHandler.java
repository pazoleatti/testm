package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClosePeriodHandler extends AbstractActionHandler<ClosePeriodAction, ClosePeriodResult> {

	@Autowired
	private ReportPeriodService reportPeriodService;

	public ClosePeriodHandler() {
		super(ClosePeriodAction.class);
	}

	@Override
	public ClosePeriodResult execute(ClosePeriodAction action, ExecutionContext executionContext) throws ActionException {
		ClosePeriodResult result = new ClosePeriodResult();
		reportPeriodService.closePeriod(action.getReportPeriodId());
		return result;
	}

	@Override
	public void undo(ClosePeriodAction getPeriodDataAction, ClosePeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
