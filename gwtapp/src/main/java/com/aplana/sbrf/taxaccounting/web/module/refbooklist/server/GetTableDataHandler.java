package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.Type;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
@Component("getRefBookListTableDataHandler")
public class GetTableDataHandler extends AbstractActionHandler<GetTableDataAction, GetTableDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    RefBookFactory refBookFactory;

    public GetTableDataHandler() {
        super(GetTableDataAction.class);
    }

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();
        List<RefBook> list = new ArrayList<RefBook>();

        if (Type.EXTERNAL.equals(action.getType())) {
            // TODO внешние
        } else if (Type.INTERNAL.equals(action.getType())) {
            // TODO  внутренние
        } else {
            list = refBookFactory.getAll();
        }

        List<TableModel> returnList = new ArrayList<TableModel>();
        boolean isFiltered = action.getFilter() != null && !action.getFilter().isEmpty();
        for (RefBook model : list) {
            if (!isFiltered || model.getName().toLowerCase().contains(action.getFilter().toLowerCase())) {
                // TODO внеш/внутр
                returnList.add(new TableModel(model.getId(), model.getName(), Type.EXTERNAL));
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
