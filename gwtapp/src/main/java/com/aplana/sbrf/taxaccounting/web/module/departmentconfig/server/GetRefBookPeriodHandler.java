package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetRefBookPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetRefBookPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetRefBookPeriodHandler extends AbstractActionHandler<GetRefBookPeriodAction, GetRefBookPeriodResult> {

    public GetRefBookPeriodHandler() {
        super(GetRefBookPeriodAction.class);
    }

    @Autowired
    private PeriodService reportService;

    @Override
    public GetRefBookPeriodResult execute(GetRefBookPeriodAction action, ExecutionContext context) throws ActionException {
        GetRefBookPeriodResult result = new GetRefBookPeriodResult();
        ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
        result.setStartDate(period.getCalendarStartDate());
        result.setEndDate(period.getEndDate());
        return result;
    }

    @Override
    public void undo(GetRefBookPeriodAction action, GetRefBookPeriodResult result, ExecutionContext context) throws ActionException {

    }
}
