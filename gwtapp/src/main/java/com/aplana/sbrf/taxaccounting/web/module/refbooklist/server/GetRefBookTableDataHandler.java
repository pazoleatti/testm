package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS', 'N_ROLE_CONF', 'F_ROLE_CONF')")
@Component
public class GetRefBookTableDataHandler extends AbstractActionHandler<GetTableDataAction, GetTableDataResult> {

    @Autowired
    RefBookFactory refBookFactory;

    public GetRefBookTableDataHandler() {
        super(GetTableDataAction.class);
    }

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        List<RefBook> list = refBookFactory.getAll(action.isOnlyVisible());

        List<TableModel> returnList = new ArrayList<TableModel>();
        boolean isFiltered = action.getFilter() != null && !action.getFilter().isEmpty();
        int rowNum = 0;
        for (RefBook refBook : list) {
            if (!isFiltered || refBook.getName().toLowerCase().contains(action.getFilter().toLowerCase())) {
                returnList.add(new TableModel(refBook.getId(), rowNum++, refBook.getName(), RefBookType.get(refBook.getType()),
                        refBook.isReadOnly(), refBook.isVisible(), refBook.getRegionAttribute()));
            }
        }

        result.setTableData(returnList);
        return result;
    }

    @Override
    public void undo(GetTableDataAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
