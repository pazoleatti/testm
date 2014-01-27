package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetTableDataHandler extends AbstractActionHandler<GetTableDataAction, GetTableDataResult> {

    public GetTableDataHandler() {
        super(GetTableDataAction.class);
    }

    @Autowired
    private SourceService departmentFormTypeService;

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        char taxType = action.getTaxType();
        List<FormTypeKind> data = new ArrayList<FormTypeKind>();
        if (action.isForm()){
            for (Integer depoId: action.getDepartmentsIds()){
                data.addAll(departmentFormTypeService.getFormAssigned(depoId.longValue(), taxType));
            }
        }
        else {
            for (Integer depoId: action.getDepartmentsIds()){
                data.addAll(departmentFormTypeService.getDeclarationAssigned(depoId.longValue(), taxType));
            }
        }
        result.setTableData(data);
        return result;
    }

    @Override
    public void undo(GetTableDataAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
