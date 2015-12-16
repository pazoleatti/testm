package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AssignFormsAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AssignFormsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author auldanov
 */

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AssignFormsHolder extends AbstractActionHandler<AssignFormsAction, AssignFormsResult> {
    @Autowired
    private SourceService departmentFormTypeService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private FormTypeService formTypeService;

    public AssignFormsHolder() {
        super(AssignFormsAction.class);
    }

    @Override
    public AssignFormsResult execute(AssignFormsAction action, ExecutionContext executionContext) throws ActionException {
        boolean detectRelations = false;
        AssignFormsResult result = new AssignFormsResult();
        Logger logger = new Logger();

        for (Integer departmentId : action.getDepartments()){
            for (Long formTypeId: action.getFormTypes()){
                // Для полученного сочетания "Подразделение-Тип налоговой формы-Вид налоговой формы", Система проверяет наличие этого сочетания в таблице БД "Назначение НФ подразделениям". Сочетание НЕ существует.
                if (departmentFormTypeService.existAssignedForm(departmentId, formTypeId.intValue(), action.getFormDataKind())) {
                    detectRelations = true;

                    // сообщение в лог
                    Department department = departmentService.getDepartment(departmentId);
                    FormType formType = formTypeService.get(formTypeId.intValue());
                    logger.warn("Для \""+department.getName()+"\" уже существует назначение \""+action.getFormDataKind().getTitle()+"\" - \""+formType.getName() + "\"");
                } else {
                    // сохранение
                    departmentFormTypeService.saveDFT(departmentId.longValue(), formTypeId.intValue(), action.getFormDataKind().getId(), action.getPerformers());
                }
            }
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setIssetRelations(detectRelations);
        return result;
    }

    @Override
    public void undo(AssignFormsAction assignFormsAction, AssignFormsResult assignFormsResult, ExecutionContext executionContext) throws ActionException {

    }
}

