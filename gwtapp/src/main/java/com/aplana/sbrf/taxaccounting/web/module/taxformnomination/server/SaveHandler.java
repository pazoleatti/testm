package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.SaveAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveHandler extends AbstractActionHandler<SaveAction, GetTableDataResult>{

    public SaveHandler() {
        super(SaveAction.class);
    }

    @Autowired
    private SourceService departmentFormTypeService;

    @Override
    public GetTableDataResult execute(SaveAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        Set<Long> ids = action.getIds();
        Long departmentId = action.getDepartmentId();
        char taxType = action.getTaxType();

        if (action.isForm()) {// "Налоговые формы"
            if (ids == null) {
                // Сохранение
                departmentFormTypeService.saveDFT(departmentId, action.getTypeId(), action.getFormId());
            } else {
                // Удаление
                departmentFormTypeService.deleteDFT(ids);
            }
            // актуальный список
            result.setTableData(departmentFormTypeService.getFormAssigned(departmentId, taxType));

        } else {// "Декларации"
            if (ids == null) {
                // Сохранение
                departmentFormTypeService.saveDDT(departmentId, action.getFormId());
            } else {
                // Удаление
                departmentFormTypeService.deleteDDT(ids);
            }
            // актуальный список
            result.setTableData(departmentFormTypeService.getDeclarationAssigned(departmentId, taxType));
        }
        return result;
    }

    @Override
    public void undo(SaveAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
