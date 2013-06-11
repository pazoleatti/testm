package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

public class ClosePeriodHandler extends AbstractActionHandler<ClosePeriodAction, ClosePeriodResult> {

	public ClosePeriodHandler() {
		super(ClosePeriodAction.class);
	}

	@Override
	public ClosePeriodResult execute(ClosePeriodAction action, ExecutionContext executionContext) throws ActionException {
		ClosePeriodResult result = new ClosePeriodResult();

		return result;
	}

	@Override
	public void undo(ClosePeriodAction getPeriodDataAction, ClosePeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
