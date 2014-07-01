package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentAssingsAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.DeparmentFormTypeAssembler;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCurrentAssignsHandler extends
        AbstractActionHandler<GetCurrentAssingsAction, GetCurrentSourcesResult> {

    @Autowired
    private SourceService departmentFormTypeService;

    @Autowired
    private DeparmentFormTypeAssembler deparmentFormTypeAssembler;

    public GetCurrentAssignsHandler() {
        super(GetCurrentAssingsAction.class);
    }

    @Override
    public GetCurrentSourcesResult execute(GetCurrentAssingsAction action, ExecutionContext context) throws ActionException {
        GetCurrentSourcesResult result = new GetCurrentSourcesResult();

        Calendar periodFrom = Calendar.getInstance();
        periodFrom.setTime(action.getPeriodsInterval().getPeriodFrom().getStartDate());
        periodFrom.set(Calendar.YEAR, action.getPeriodsInterval().getYearFrom());

        Calendar periodTo = Calendar.getInstance();
        periodTo.setTime(action.getPeriodsInterval().getPeriodTo().getEndDate());
        periodTo.set(Calendar.YEAR, action.getPeriodsInterval().getYearTo());
        if(action.isForm()){
            List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
                    .getDFTSourcesByDFT(action.getDepartmentId(), action.getTypeId(), action.getKind(), periodFrom.getTime(), periodTo.getTime());
            result.setCurrentSources(deparmentFormTypeAssembler.assemble(departmentFormTypes));
        } else {
            List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
                    .getDFTSourceByDDT(action.getDepartmentId(), action.getTypeId(), periodFrom.getTime(), periodTo.getTime());
            result.setCurrentSources(deparmentFormTypeAssembler.assemble(departmentFormTypes));
        }

        return result;
    }

    @Override
    public void undo(GetCurrentAssingsAction arg0, GetCurrentSourcesResult arg1, ExecutionContext arg2)
            throws ActionException {
        // Nothing!
    }
}
