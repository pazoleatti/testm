package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.DeleteIfrsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.DeleteIfrsDataResult;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class DeleteIfrsDataHandler extends AbstractActionHandler<DeleteIfrsDataAction, DeleteIfrsDataResult> {

    @Autowired
    IfrsDataService ifrsDataService;

    public DeleteIfrsDataHandler() {
        super(DeleteIfrsDataAction.class);
    }

    @Override
    public DeleteIfrsDataResult execute(DeleteIfrsDataAction action, ExecutionContext context) throws ActionException {
        List<Integer> reportPeriodIds = new ArrayList<Integer>();
        for (IfrsRow row : action.getReportPeriodIds()) {
            reportPeriodIds.add(row.getReportPeriodId());
        }
        ifrsDataService.delete(reportPeriodIds);
        return null;
    }

    @Override
    public void undo(DeleteIfrsDataAction action, DeleteIfrsDataResult result, ExecutionContext context) throws ActionException {

    }
}
