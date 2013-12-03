package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.server;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessListAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessListResult;
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
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetHistoryBusinessListHandler extends AbstractActionHandler<GetHistoryBusinessListAction, GetHistoryBusinessListResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    LogBusinessService logBusinessService;

    public GetHistoryBusinessListHandler() {
        super(GetHistoryBusinessListAction.class);
    }

    @Override
    public GetHistoryBusinessListResult execute(GetHistoryBusinessListAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<LogSearchResultItem> resultItems = logBusinessService.getLogsBusiness(userInfo, action.getFilterValues());
        GetHistoryBusinessListResult result = new GetHistoryBusinessListResult();
        result.setRecords(resultItems);
        result.setTotalCountOfRecords(resultItems.getTotalCount());
        return result;
    }

    @Override
    public void undo(GetHistoryBusinessListAction action, GetHistoryBusinessListResult result, ExecutionContext context) throws ActionException {
        //Not implemented
    }
}
