package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
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

import static java.util.Arrays.asList;

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
    @Autowired
    private DepartmentService departmentService;

    public GetIfrsReportPeriodsHandler() {
        super(GetReportPeriodsAction.class);
    }

    @Override
    public GetReportPeriodsResult execute(GetReportPeriodsAction action, ExecutionContext executionContext) throws ActionException {
        GetReportPeriodsResult result = new GetReportPeriodsResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
        periodList.addAll(periodService.getPeriodsByTaxTypeAndDepartments(TaxType.INCOME,
                new ArrayList<Integer>(departmentService.getTaxFormDepartments(userInfo.getUser(),
                        TaxType.INCOME, null, null))));
        result.setReportPeriods(periodList);
        return result;
    }

    @Override
    public void undo(GetReportPeriodsAction getReportPeriodsAction, GetReportPeriodsResult getReportPeriodsResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
