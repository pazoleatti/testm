package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
@Component
public class GetRefBookTableDataHandler extends AbstractActionHandler<GetTableDataAction, GetTableDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    RefBookFactory refBookFactory;

    public GetRefBookTableDataHandler() {
        super(GetTableDataAction.class);
    }

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        List<RefBook> list = refBookFactory.getAll(true, action.getType()); // запросить только видимые справочники

        List<TableModel> returnList = new ArrayList<TableModel>();
        boolean isFiltered = action.getFilter() != null && !action.getFilter().isEmpty();
        for (RefBook model : list) {
            if (!isFiltered || model.getName().toLowerCase().contains(action.getFilter().toLowerCase())) {
                returnList.add(new TableModel(model.getId(), model.getName(), RefBookType.EXTERNAL));
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
