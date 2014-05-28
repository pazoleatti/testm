package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsFilter;
import com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBookerStatementsListAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBookerStatementsListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetBookerStatementsListHandler extends AbstractActionHandler<GetBookerStatementsListAction, GetBookerStatementsListResult> {

    public GetBookerStatementsListHandler() {
        super(GetBookerStatementsListAction.class);
    }

    @Autowired
    private BookerStatementsService bookerStatementsService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public GetBookerStatementsListResult execute(GetBookerStatementsListAction action, ExecutionContext executionContext) throws ActionException {
        GetBookerStatementsListResult result = new GetBookerStatementsListResult();

        PagingResult<BookerStatementsSearchResultItem> resultPage = bookerStatementsService.findDataByFilter(action.getFilter());

        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        for(BookerStatementsSearchResultItem item: resultPage) {
            if (departmentFullNames.get(item.getDepartmentId()) == null) departmentFullNames.put(item.getDepartmentId(), departmentService.getParentsHierarchyShortNames(item.getDepartmentId()));
        }
        result.setDepartmentFullNames(departmentFullNames);
        result.setDataRows(resultPage);
        result.setTotalCount(resultPage.getTotalCount());
        return result;
    }

    @Override
    public void undo(GetBookerStatementsListAction action, GetBookerStatementsListResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
