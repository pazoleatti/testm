package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentParamAliases;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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
