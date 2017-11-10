package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDeclarationTypeForCreateHandler extends AbstractActionHandler<GetDeclarationTypeAction, GetDeclarationTypeResult> {

    public GetDeclarationTypeForCreateHandler() {
        super(GetDeclarationTypeAction.class);
    }

    @Autowired
    DeclarationTypeService declarationTypeService;

    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    PeriodService periodService;

    @Autowired
    RefBookFactory rbFactory;

    @Override
    public GetDeclarationTypeResult execute(GetDeclarationTypeAction action, ExecutionContext executionContext) throws ActionException {
        GetDeclarationTypeResult result = new GetDeclarationTypeResult();
        result.setDeclarationTypes(declarationTypeService.getTypes(action.getDepartmentId(), action.getReportPeriod(), action.getTaxType(), Arrays.asList(action.getDeclarationFormKind())));
        result.setCorrectionDate(departmentReportPeriodService.getLast(action.getDepartmentId(), action.getReportPeriod()).getCorrectionDate());
        result.setTaxType(action.getTaxType());
        return result;
    }

    @Override
    public void undo(GetDeclarationTypeAction getDeclarationTypeAction, GetDeclarationTypeResult getDeclarationTypeResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
