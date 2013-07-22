package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.SaveAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

        Set<Long> ids = action.getIds();
        Long departmentId = action.getDepartmentId();
        char taxType = action.getTaxType();

        if (action.isForm()) {// "Налоговые формы"
            if (ids == null) {
                int formId = action.getFormId();
                int typeId = action.getTypeId();
                // Сохранение
                List<FormTypeKind> list = departmentFormTypeService.getFormAssigned(departmentId, taxType);
                for (FormTypeKind model : list) {
                    if (model.getFormTypeId().intValue() == formId && model.getKind().getId() == typeId) {
                        // дубль не сохраняем
                        result.setErrorOnSave("Налоговая форма указанного типа и вида уже назначена подразделению");
                        return result;
                    }
                }
                departmentFormTypeService.saveForm(departmentId, typeId, formId);
            } else {
                // Удаление
                try {
                    departmentFormTypeService.deleteForm(ids);
                } catch (DataIntegrityViolationException exception) {
                    // есть зависимые связи
                    result.setErrorOnSave("Невозможно снять назначение налоговой формы, т.к. определены источники или приёмники данных");
                    return result;
                }
            }
            // актуальный список
            result.setTableData(departmentFormTypeService.getFormAssigned(departmentId, taxType));

        } else {// "Декларации"
            if (ids == null) {
                int formId = action.getFormId();
                // Сохранение
                List<FormTypeKind> list = departmentFormTypeService.getDeclarationAssigned(departmentId, taxType);
                for (FormTypeKind model : list) {
                    if (model.getFormTypeId().intValue() == formId) {
                        result.setErrorOnSave("Декларация указанного вида уже назначена подразделению");
                        return result;
                    }
                }
                departmentFormTypeService.saveDeclaration(departmentId, formId);
            } else {
                // Удаление
                try {
                    departmentFormTypeService.deleteDeclaration(ids);
                } catch (DataIntegrityViolationException exception) {
                    // есть зависимые связи
                    result.setErrorOnSave("Невозможно снять назначение декларации, т.к. определены источники или приёмники данных");
                    return result;
                }
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
