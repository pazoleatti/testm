package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListResult;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.LogSystemAuditFilter;
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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetAuditDataListHandler extends AbstractActionHandler<GetAuditDataListAction, GetAuditDataListResult> {

    @Autowired
    AuditService auditService;
    @Autowired
    SecurityService securityService;

    public GetAuditDataListHandler() {
        super(GetAuditDataListAction.class);
    }

    @Override
    public GetAuditDataListResult execute(GetAuditDataListAction action, ExecutionContext executionContext) throws ActionException {

        GetAuditDataListResult result = new GetAuditDataListResult();
        LogSystemAuditFilter auditFilter = action.getLogSystemFilter();
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<LogSearchResultItem> records;
        if (userInfo.getUser().hasRole(TARole.N_ROLE_ADMIN))
		    records = auditService.getLogsByFilter(auditFilter.convertTo());
        else
            records = auditService.getLogsBusiness(auditFilter.convertTo(), userInfo);
        result.setRecords(records);
		result.setTotalCountOfRecords(records.getTotalCount());

        return result;
    }

    @Override
    public void undo(GetAuditDataListAction getAuditDataListAction, GetAuditDataListResult getAuditDataListResult, ExecutionContext executionContext) throws ActionException {

    }
}
