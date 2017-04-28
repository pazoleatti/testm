package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetTaxFormTypesHandler extends AbstractActionHandler<GetTaxFormTypesAction, GetTaxFormTypesResult> {

    public GetTaxFormTypesHandler() {
        super(GetTaxFormTypesAction.class);
    }

    @Autowired
    private SourceService sourceService;

    @Override
    public GetTaxFormTypesResult execute(GetTaxFormTypesAction action, ExecutionContext executionContext) throws ActionException {
        GetTaxFormTypesResult res = new GetTaxFormTypesResult();
        List<DeclarationType> declarationTypeList = sourceService.allDeclarationTypeByTaxType(action.getTaxType());
        res.setFormTypeList(declarationTypeList);
        return res;
    }

    @Override
    public void undo(GetTaxFormTypesAction getTaxFormTypesAction, GetTaxFormTypesResult getTaxFormTypesResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
