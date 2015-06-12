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
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetRefBookPeriodHandler extends AbstractActionHandler<GetRefBookPeriodAction, GetRefBookPeriodResult> {

    public GetRefBookPeriodHandler() {
        super(GetRefBookPeriodAction.class);
    }

    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private PeriodService reportService;

    @Override
    public GetRefBookPeriodResult execute(GetRefBookPeriodAction action, ExecutionContext context) throws ActionException {
        GetRefBookPeriodResult result = new GetRefBookPeriodResult();
        Long refBookId = null;
        switch (action.getTaxType()) {
            case INCOME:
                refBookId = RefBook.DEPARTMENT_CONFIG_INCOME;
                break;
            case TRANSPORT:
                refBookId = RefBook.DEPARTMENT_CONFIG_TRANSPORT;
                break;
            case DEAL:
                refBookId = RefBook.DEPARTMENT_CONFIG_DEAL;
                break;
            case VAT:
                refBookId = RefBook.DEPARTMENT_CONFIG_VAT;
                break;
            case PROPERTY:
                refBookId = RefBook.DEPARTMENT_CONFIG_PROPERTY;
                break;
        }
        RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);
        ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
        String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentId();
        result.setStartDate(period.getCalendarStartDate());
        //Дата окончания = дате начала следующей версии
        result.setEndDate(provider.getNextVersion(period.getCalendarStartDate(), filter));
        return result;
    }

    @Override
    public void undo(GetRefBookPeriodAction action, GetRefBookPeriodResult result, ExecutionContext context) throws ActionException {

    }
}
