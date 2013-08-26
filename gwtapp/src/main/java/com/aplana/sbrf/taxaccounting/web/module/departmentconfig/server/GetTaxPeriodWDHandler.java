package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
    TaxPeriodDao taxPeriodSDao;

    @Autowired
    ReportPeriodDao reportPeriodDao;

    @Autowired
    ReportPeriodService reportPeriodService;

    public GetTaxPeriodWDHandler() {
        super(GetTaxPeriodWDAction.class);
    }

    @Override
    public GetTaxPeriodWDResult execute(GetTaxPeriodWDAction action, ExecutionContext executionContext)
            throws ActionException {
        GetTaxPeriodWDResult result = new GetTaxPeriodWDResult();
        result.setTaxPeriods(taxPeriodSDao.listByTaxType(action.getTaxType()));
        result.setLastReportPeriod(reportPeriodService.getLastReportPeriod(action.getTaxType(), action.getDepartmentId()));
        return result;
    }

    @Override
    public void undo(GetTaxPeriodWDAction action, GetTaxPeriodWDResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
