package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class OpenPeriodHandler extends AbstractActionHandler<OpenPeriodAction, OpenPeriodResult> {

	@Autowired
	private PeriodService reportPeriodService;

	public OpenPeriodHandler() {
		super(OpenPeriodAction.class);
	}

	@Override
	public OpenPeriodResult execute(OpenPeriodAction action, ExecutionContext executionContext) throws ActionException {
		DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setReportPeriod(new ReportPeriod());
		departmentReportPeriod.getReportPeriod().setTaxPeriod(new TaxPeriod());
		departmentReportPeriod.getReportPeriod().getTaxPeriod().setYear(action.getYear());
		departmentReportPeriod.getReportPeriod().setDictTaxPeriodId(action.getDictionaryTaxPeriodId());
		departmentReportPeriod.setDepartmentId(action.getDepartmentId());
		departmentReportPeriod.setCorrectionDate(action.getCorrectPeriod());
		OpenPeriodResult result = new OpenPeriodResult();
        result.setUuid(reportPeriodService.open(departmentReportPeriod));
		return result;
	}

	@Override
	public void undo(OpenPeriodAction getPeriodDataAction, OpenPeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
