package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;


import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
                    declarationDataSearchService.getFilterAvailableValues(userInfo, action.getTaxType(), false);
            reportPeriods.addAll(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(),
                    new ArrayList<Integer>(declarationFilterValues.getDepartmentIds())));
        } else {
            reportPeriods.addAll(periodService.getOpenForUser(userInfo.getUser(), action.getTaxType()));
        }

        result.setReportPeriods(initReportPeriodModelList(reportPeriods));

        if (action.getReportPeriodId() != null) {
            // проверяем доступность для пользователя
            for(ReportPeriodViewModel reportPeriod: initReportPeriodModelList(reportPeriods))
                if (reportPeriod.getId().equals(action.getReportPeriodId())) {
                    result.setDefaultReportPeriod(reportPeriod);
                    break;
                }
        }
        if (result.getDefaultReportPeriod() == null) {
            if (reportPeriods != null && !reportPeriods.isEmpty()) {
                ReportPeriod maxPeriod = reportPeriods.get(0);
                for (ReportPeriod per : reportPeriods) {
                    if (per.getEndDate().isAfter(maxPeriod.getEndDate())) {
                        maxPeriod = per;
                    }
                }
                result.setDefaultReportPeriod(toReportPeriod(maxPeriod));
            }
        }
        return result;
	}

	@Override
	public void undo(GetReportPeriodsAction getTaxPeriods, GetReportPeriodsResult getTaxPeriodsResult, ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}

    private List<ReportPeriodViewModel> initReportPeriodModelList(List<ReportPeriod> reportPeriods) {
        List<ReportPeriodViewModel> models = new ArrayList<>();
        for (ReportPeriod period : reportPeriods){
            models.add(new ReportPeriodViewModel(period));
        }
        return models;
    }

    public ReportPeriodViewModel toReportPeriod(ReportPeriod selectedPeriod) {
        ReportPeriodViewModel reportPeriod = new ReportPeriodViewModel();
        reportPeriod.setId(selectedPeriod.getId());
        reportPeriod.setName(selectedPeriod.getName());
        reportPeriod.setAccName(selectedPeriod.getAccName());
        reportPeriod.setOrder(selectedPeriod.getOrder());
        reportPeriod.setTaxPeriod(selectedPeriod.getTaxPeriod());
        reportPeriod.setStartDate(selectedPeriod.getStartDate().toDate());
        reportPeriod.setEndDate(selectedPeriod.getEndDate().toDate());
        reportPeriod.setCalendarStartDate(selectedPeriod.getCalendarStartDate().toDate());
        reportPeriod.setDictTaxPeriodId(selectedPeriod.getDictTaxPeriodId());
        reportPeriod.setPermissions(selectedPeriod.getPermissions());
        return reportPeriod;
    }


}
