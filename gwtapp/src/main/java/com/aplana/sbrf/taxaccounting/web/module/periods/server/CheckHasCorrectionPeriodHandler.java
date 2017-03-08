package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasCorrectionPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class CheckHasCorrectionPeriodHandler extends AbstractActionHandler<CheckHasCorrectionPeriodAction, CheckHasCorrectionPeriodResult> {

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    public CheckHasCorrectionPeriodHandler() {
        super(CheckHasCorrectionPeriodAction.class);
    }

    @Override
    public CheckHasCorrectionPeriodResult execute(CheckHasCorrectionPeriodAction action, ExecutionContext executionContext) throws ActionException {
        CheckHasCorrectionPeriodResult result = new CheckHasCorrectionPeriodResult();
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setIsCorrection(true);
        filter.setDepartmentIdList(Arrays.asList(action.getDepartmentId()));
        filter.setTaxTypeList(Arrays.asList(action.getTaxType()));
        filter.setReportPeriodIdList(Arrays.asList(action.getReportPeriodId()));
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
        result.setHasCorrectionPeriods(!departmentReportPeriodList.isEmpty());
        return result;
    }

    @Override
    public void undo(CheckHasCorrectionPeriodAction checkHasCorrectionPeriodAction,
                     CheckHasCorrectionPeriodResult checkHasCorrectionPeriodResult,
                     ExecutionContext executionContext) throws ActionException {
    }
}
