package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriods;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_ADMIN')")
public class GetReportPeriodsHandler extends AbstractActionHandler<GetReportPeriods, GetReportPeriodsResult> {

	public GetReportPeriodsHandler() {
		super(GetReportPeriods.class);
	}

	@Autowired
	private PeriodService reportPeriodService;

	@Override
	public GetReportPeriodsResult execute(GetReportPeriods action, ExecutionContext executionContext) throws ActionException {
		GetReportPeriodsResult result = new GetReportPeriodsResult();
	    result.setReportPeriods(reportPeriodService.listByTaxPeriod(action.getTaxPeriod().getId()));
		return result;
	}

	@Override
	public void undo(GetReportPeriods getTaxPeriods, GetReportPeriodsResult getTaxPeriodsResult, ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}
}
