package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.DepartmentDeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetTaxFormTypesHandler extends AbstractActionHandler<GetTaxFormTypesAction, GetTaxFormTypesResult> {

    public GetTaxFormTypesHandler() {
        super(GetTaxFormTypesAction.class);
    }

    @Autowired
    private DepartmentFormTypeService departmentFormTypeService;
    
    @Autowired
    private DepartmentDeclarationTypeService departmentDeclarationTypeService;

    @Override
    public GetTaxFormTypesResult execute(GetTaxFormTypesAction action, ExecutionContext executionContext) throws ActionException {
        GetTaxFormTypesResult res = new GetTaxFormTypesResult();
        List<FormType> resultList = new ArrayList<FormType>();
        if (action.isForm()) {
             resultList = departmentFormTypeService.listAllByTaxType(action.getTaxType());
        }
        else {
            List<DeclarationType> declarationTypeList = departmentDeclarationTypeService.listAllByTaxType(action.getTaxType());
            for (DeclarationType item : declarationTypeList){
                FormType m = new FormType();
                m.setId(item.getId());
                m.setName(item.getName());
                m.setTaxType(item.getTaxType());
                resultList.add(m);
            }
        }
        res.setFormTypeList(resultList);
        return res;
    }

    @Override
    public void undo(GetTaxFormTypesAction getTaxFormTypesAction, GetTaxFormTypesResult getTaxFormTypesResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
