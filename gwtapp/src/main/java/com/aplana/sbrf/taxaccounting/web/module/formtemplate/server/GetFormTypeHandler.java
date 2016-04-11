package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.service.StyleService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFormTypeHandler extends AbstractActionHandler<GetFormTypeAction, GetFormTypeResult> {

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private StyleService styleService;

    public GetFormTypeHandler() {
        super(GetFormTypeAction.class);
    }

    @Override
    public GetFormTypeResult execute(GetFormTypeAction action, ExecutionContext context) throws ActionException {
        GetFormTypeResult result = new GetFormTypeResult();
        result.setFormType(formTypeService.get(action.getFormTypeId()));
        result.setRefBookList(refBookFactory.getAll(false));
        result.setStyles(styleService.getAll());
        return result;
    }

    @Override
    public void undo(GetFormTypeAction action, GetFormTypeResult result, ExecutionContext context) throws ActionException {

    }
}
