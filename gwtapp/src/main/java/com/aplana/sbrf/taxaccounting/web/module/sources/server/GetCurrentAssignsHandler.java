package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentAssignsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.DeparmentFormTypeAssembler;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCurrentAssignsHandler extends
        AbstractActionHandler<GetCurrentAssignsAction, GetCurrentAssignsResult> {

    @Autowired
    private SourceService departmentFormTypeService;

    @Autowired
    private DeparmentFormTypeAssembler deparmentFormTypeAssembler;

    public GetCurrentAssignsHandler() {
        super(GetCurrentAssignsAction.class);
    }

    @Override
    public GetCurrentAssignsResult execute(GetCurrentAssignsAction action, ExecutionContext context) throws ActionException {
        GetCurrentAssignsResult result = new GetCurrentAssignsResult();

        Date periodFrom = PeriodConvertor.getDateFrom(action.getPeriodsInterval());
        Date periodTo = PeriodConvertor.getDateTo(action.getPeriodsInterval());
        if(!action.isDeclaration()){
            List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
                    .getDFTSourcesByDFT(action.getDepartmentId(), action.getTypeId(), action.getKind(), periodFrom, periodTo);
            result.setCurrentSources(deparmentFormTypeAssembler.assemble(departmentFormTypes));
        } else {
            List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
                    .getDFTSourceByDDT(action.getDepartmentId(), action.getTypeId(), periodFrom, periodTo);
            result.setCurrentSources(deparmentFormTypeAssembler.assemble(departmentFormTypes));
        }

        return result;
    }

    @Override
    public void undo(GetCurrentAssignsAction arg0, GetCurrentAssignsResult arg1, ExecutionContext arg2)
            throws ActionException {
        // Nothing!
    }
}
