package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.SaveAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class SaveHandler extends AbstractActionHandler<SaveAction, GetTableDataResult> {

    public SaveHandler() {
        super(SaveAction.class);
    }

    @Autowired
    private DepartmentFormTypeService departmentFormTypeService;

    @Override
    public GetTableDataResult execute(SaveAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();
        result.setTableData(departmentFormTypeService.save(action.getIds(), action.getDepartmentId(), action.getTypeId(),
                action.getFormId(), action.getTaxType(), action.isForm()));
        return result;
    }

    @Override
    public void undo(SaveAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
