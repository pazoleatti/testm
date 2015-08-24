package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.GetIfrsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.GetIrfsDataResult;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetIfrsDataHandler extends AbstractActionHandler<GetIfrsDataAction, GetIrfsDataResult> {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private IfrsDataService ifrsDataService;


    public GetIfrsDataHandler() {
        super(GetIfrsDataAction.class);
    }

    @Override
    public GetIrfsDataResult execute(GetIfrsDataAction action, ExecutionContext executionContext) throws ActionException {

        GetIrfsDataResult result = new GetIrfsDataResult();
        PagingResult<IfrsRow> pagingResult = new PagingResult<IfrsRow>();
        PagingResult<IfrsDataSearchResultItem> records = ifrsDataService.findByReportPeriod(action.getReportPeriodIds(), action.getPagingParams());
        for(IfrsDataSearchResultItem record: records) {
            IfrsRow row = new IfrsRow();
            row.setReportPeriodId(record.getReportPeriodId());
            row.setPeriodName(record.getPeriodName());
            row.setYear(record.getYear());
            if (record.getBlobDataId() != null) {
                row.setStatus(IfrsRow.StatusIfrs.EXIST);
            } else {
                if (lockDataService.getLock(ifrsDataService.generateTaskKey(record.getReportPeriodId())) != null) {
                    row.setStatus(IfrsRow.StatusIfrs.LOCKED);
                } else {
                    row.setStatus(IfrsRow.StatusIfrs.NOT_EXIST);
                }
            }
            pagingResult.add(row);
        }
        pagingResult.setTotalCount(records.getTotalCount());
        result.setTotalCountOfRecords(records.size());
        result.setIfrsRows(pagingResult);

        return result;
    }

    @Override
    public void undo(GetIfrsDataAction action, GetIrfsDataResult result, ExecutionContext executionContext) throws ActionException {

    }
}
