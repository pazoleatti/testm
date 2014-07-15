package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class EditFormTypeHandler extends AbstractActionHandler<EditFormTypeAction, EditFormTypeResult> {

    public EditFormTypeHandler() {
        super(EditFormTypeAction.class);
    }

    @Autowired
    private FormTypeService formTypeService;

    @Override
    public EditFormTypeResult execute(EditFormTypeAction action, ExecutionContext executionContext) throws ActionException {
        String code = action.getNewFormTypeCode();
        if (code != null || !"".equals(code)) {
            FormType formType = formTypeService.getByCode(code);
            if (formType != null && formType.getId() != action.getFormTypeId()) {
                throw new ActionException("Нарушено требование к уникальности, уже существует макет с такими значениями атрибута CODE!");
            }
        }
        formTypeService.updateFormType(action.getFormTypeId(), action.getNewFormTypeName(), action.getNewFormTypeCode());
        return new EditFormTypeResult();
    }

    @Override
    public void undo(EditFormTypeAction editFormTypeAction, EditFormTypeResult editFormTypeResult, ExecutionContext executionContext) throws ActionException {

    }
}
