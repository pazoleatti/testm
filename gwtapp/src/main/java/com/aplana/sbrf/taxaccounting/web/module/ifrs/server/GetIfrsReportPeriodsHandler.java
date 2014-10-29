package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.GetReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.GetReportPeriodsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetIfrsReportPeriodsHandler extends AbstractActionHandler<GetReportPeriodsAction, GetReportPeriodsResult> {

    @Autowired
    PeriodService periodService;
    @Autowired
    SecurityService securityService;

    public GetIfrsReportPeriodsHandler() {
        super(GetReportPeriodsAction.class);
    }

    @Override
    public GetReportPeriodsResult execute(GetReportPeriodsAction getReportPeriodsAction, ExecutionContext executionContext) throws ActionException {
        GetReportPeriodsResult result = new GetReportPeriodsResult();
        List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
        periodList.addAll(periodService.getOpenForUser(securityService.currentUserInfo().getUser(), TaxType.INCOME));
        result.setReportPeriods(periodList);
        return result;
    }

    @Override
    public void undo(GetReportPeriodsAction getReportPeriodsAction, GetReportPeriodsResult getReportPeriodsResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
