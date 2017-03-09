package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckPeriodStatusAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckPeriodStatusResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class CheckPeriodStatusHandler extends AbstractActionHandler<CheckPeriodStatusAction, CheckPeriodStatusResult> {


	public CheckPeriodStatusHandler() {
		super(CheckPeriodStatusAction.class);
	}
	@Autowired
	PeriodService periodService;

	@Override
	public CheckPeriodStatusResult execute(CheckPeriodStatusAction action, ExecutionContext executionContext) throws ActionException {
		CheckPeriodStatusResult result = new CheckPeriodStatusResult();
		result.setStatus(periodService.checkPeriodStatusBeforeOpen(action.getTaxType(), action.getYear(),
				action.isBalancePeriod(), action.getDepartmentId(), action.getDictionaryTaxPeriodId()));
		return result;

	}

	@Override
	public void undo(CheckPeriodStatusAction action, CheckPeriodStatusResult result, ExecutionContext executionContext) throws ActionException {
	}
}