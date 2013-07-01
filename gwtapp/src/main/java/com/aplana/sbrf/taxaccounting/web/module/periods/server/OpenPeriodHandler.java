package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenPeriodHandler extends AbstractActionHandler<OpenPeriodAction, OpenPeriodResult> {

	@Autowired
	private ReportPeriodService reportPeriodService;

	public OpenPeriodHandler() {
		super(OpenPeriodAction.class);
	}

	@Override
	public OpenPeriodResult execute(OpenPeriodAction action, ExecutionContext executionContext) throws ActionException {
		OpenPeriodResult result = new OpenPeriodResult();
		reportPeriodService.openPeriod(action.getReportPeriodId());
		return result;
	}

	@Override
	public void undo(OpenPeriodAction getPeriodDataAction, OpenPeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
