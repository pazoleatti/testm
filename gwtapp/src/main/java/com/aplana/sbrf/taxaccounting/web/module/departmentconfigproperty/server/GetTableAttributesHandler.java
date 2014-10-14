package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetTableAttributesAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetTableAttributesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetTableAttributesHandler extends AbstractActionHandler<GetTableAttributesAction, GetTableAttributesResult> {

    @Autowired
    RefBookFactory rbFactory;

    public GetTableAttributesHandler() {
        super(GetTableAttributesAction.class);
    }

    @Override
    public GetTableAttributesResult execute(GetTableAttributesAction getTableAttributesAction, ExecutionContext executionContext) throws ActionException {
        List<RefBookAttribute> attributes = rbFactory.get(getTableAttributesAction.getRefBookId()).getAttributes();
        GetTableAttributesResult res = new GetTableAttributesResult();
        res.setAttributes(attributes);
        return res;
    }

    @Override
    public void undo(GetTableAttributesAction getTableAttributesAction, GetTableAttributesResult getTableAttributesResult, ExecutionContext executionContext) throws ActionException {

    }
}
