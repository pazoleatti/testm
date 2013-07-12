package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetAuditDataListHandler extends AbstractActionHandler<GetAuditDataListAction, GetAuditDataListResult> {

    @Autowired
    AuditService auditService;

    public GetAuditDataListHandler() {
        super(GetAuditDataListAction.class);
    }

    @Override
    public GetAuditDataListResult execute(GetAuditDataListAction getAuditDataListAction, ExecutionContext executionContext) throws ActionException {

        GetAuditDataListResult result = new GetAuditDataListResult();
		PagingResult<LogSystemSearchResultItem> records = auditService.getLogsByFilter(getAuditDataListAction.getLogSystemFilter());
        result.setRecords(records.getRecords());
		result.setTotalCountOfRecords(records.getTotalRecordCount());

        return result;
    }

    @Override
    public void undo(GetAuditDataListAction getAuditDataListAction, GetAuditDataListResult getAuditDataListResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
