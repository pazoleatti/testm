package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetTaxPeriodWDAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetTaxPeriodWDResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Получение параметров подразделения и списка доступных налоговых периодов
 *
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetTaxPeriodWDHandler extends AbstractActionHandler<GetTaxPeriodWDAction,
        GetTaxPeriodWDResult> {


    @Autowired
    PeriodService reportPeriodService;

    public GetTaxPeriodWDHandler() {
        super(GetTaxPeriodWDAction.class);
    }

    @Override
    public GetTaxPeriodWDResult execute(GetTaxPeriodWDAction action, ExecutionContext executionContext)
            throws ActionException {
        GetTaxPeriodWDResult result = new GetTaxPeriodWDResult();
        result.setTaxPeriods(reportPeriodService.listByTaxType(action.getTaxType()));
        DepartmentReportPeriod drp = reportPeriodService.getLastReportPeriod(action.getTaxType(), action.getDepartmentId());
        result.setLastReportPeriod(drp == null ? null : drp.getReportPeriod());
        return result;
    }

    @Override
    public void undo(GetTaxPeriodWDAction action, GetTaxPeriodWDResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
