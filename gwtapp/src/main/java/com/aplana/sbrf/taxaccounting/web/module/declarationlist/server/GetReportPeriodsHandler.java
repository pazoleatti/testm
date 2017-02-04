package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;


import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetReportPeriodsHandler extends AbstractActionHandler<GetReportPeriodsAction, GetReportPeriodsResult> {

	public GetReportPeriodsHandler() {
		super(GetReportPeriodsAction.class);
	}

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

	@Override
	public GetReportPeriodsResult execute(GetReportPeriodsAction action, ExecutionContext executionContext) throws ActionException {
		GetReportPeriodsResult result = new GetReportPeriodsResult();
        TAUserInfo userInfo = securityService.currentUserInfo();

        List<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>();
        if (action.isDownloadReports()) {
            DeclarationDataFilterAvailableValues declarationFilterValues =
                    declarationDataSearchService.getFilterAvailableValues(userInfo, action.getTaxType());
            reportPeriods.addAll(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(),
                    new ArrayList<Integer>(declarationFilterValues.getDepartmentIds())));
        } else {
            reportPeriods.addAll(periodService.getOpenForUser(userInfo.getUser(), action.getTaxType()));
        }
        result.setReportPeriods(reportPeriods);

        if (action.getReportPeriodId() != null) {
            // проверяем доступность для пользователя
            for(ReportPeriod reportPeriod: reportPeriods)
                if (reportPeriod.getId().equals(action.getReportPeriodId())) {
                    result.setDefaultReportPeriod(reportPeriod);
                    break;
                }
        }
        if (result.getDefaultReportPeriod() == null) {
            if (reportPeriods != null && !reportPeriods.isEmpty()) {
                ReportPeriod maxPeriod = reportPeriods.get(0);
                for (ReportPeriod per : reportPeriods) {
                    if (per.getEndDate().after(maxPeriod.getEndDate())) {
                        maxPeriod = per;
                    }
                }
                result.setDefaultReportPeriod(maxPeriod);
            }
        }
        return result;
	}

	@Override
	public void undo(GetReportPeriodsAction getTaxPeriods, GetReportPeriodsResult getTaxPeriodsResult, ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}
}
