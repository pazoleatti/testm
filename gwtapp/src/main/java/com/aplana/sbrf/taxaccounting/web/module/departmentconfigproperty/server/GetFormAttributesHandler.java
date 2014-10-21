package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetFormAttributesAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetFormAttributesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFormAttributesHandler extends AbstractActionHandler<GetFormAttributesAction, GetFormAttributesResult> {

    @Autowired
    RefBookFactory rbFactory;

    public GetFormAttributesHandler() {
        super(GetFormAttributesAction.class);
    }

    @Override
    public GetFormAttributesResult execute(GetFormAttributesAction getFormAttributesAction, ExecutionContext executionContext) throws ActionException {
        List<RefBookAttribute> attributes = rbFactory.get(getFormAttributesAction.getRefBookId()).getAttributes();
        GetFormAttributesResult res = new GetFormAttributesResult();
        res.setAttributes(attributes);
        List<RefBookAttribute> tableAttributes = rbFactory.get(getFormAttributesAction.getTableRefBookId()).getAttributes();
        res.setTableAttributes(tableAttributes);
        return res;
    }

    @Override
    public void undo(GetFormAttributesAction getFormAttributesAction, GetFormAttributesResult getFormAttributesResult, ExecutionContext executionContext) throws ActionException {

    }
}
