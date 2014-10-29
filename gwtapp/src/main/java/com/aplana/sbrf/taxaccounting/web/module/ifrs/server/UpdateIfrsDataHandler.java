package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.IfrsDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.UpdateStatusIfrsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.UpdateStatusIrfsDataResult;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * User: lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class UpdateIfrsDataHandler extends AbstractActionHandler<UpdateStatusIfrsDataAction, UpdateStatusIrfsDataResult> {

    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private IfrsDataService ifrsDataService;


    public UpdateIfrsDataHandler() {
        super(UpdateStatusIfrsDataAction.class);
    }

    @Override
    public UpdateStatusIrfsDataResult execute(UpdateStatusIfrsDataAction action, ExecutionContext executionContext) throws ActionException {
        UpdateStatusIrfsDataResult result = new UpdateStatusIrfsDataResult();
        Map<Integer, IfrsRow.StatusIfrs> statusMap = new HashMap<Integer, IfrsRow.StatusIfrs>();
        PagingResult<IfrsDataSearchResultItem> records = ifrsDataService.findByReportPeriod(action.getReportPeriodIds(), null);
        for(IfrsDataSearchResultItem record: records) {
            if (record.getBlobDataId() != null) {
                statusMap.put(record.getReportPeriodId(), IfrsRow.StatusIfrs.EXIST);
            } else {
                if (lockDataService.getLock(ifrsDataService.generateTaskKey(record.getReportPeriodId())) != null) {
                    statusMap.put(record.getReportPeriodId(), IfrsRow.StatusIfrs.LOCKED);
                } else {
                    statusMap.put(record.getReportPeriodId(), IfrsRow.StatusIfrs.NOT_EXIST);
                }
            }
        }
        result.setIfrsStatusMap(statusMap);
        return result;
    }

    @Override
    public void undo(UpdateStatusIfrsDataAction action, UpdateStatusIrfsDataResult result, ExecutionContext executionContext) throws ActionException {

    }
}
