package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.SearchOrderingFilter;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.SourcesAssembler;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentAssignsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCurrentAssignsHandler extends
        AbstractActionHandler<GetCurrentAssignsAction, GetCurrentAssignsResult> {

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourcesAssembler sourceAssembler;

    public GetCurrentAssignsHandler() {
        super(GetCurrentAssignsAction.class);
    }

    @Override
    public GetCurrentAssignsResult execute(GetCurrentAssignsAction action, ExecutionContext context) throws ActionException {
        GetCurrentAssignsResult result = new GetCurrentAssignsResult();

        Date periodFrom = PeriodConvertor.getDateFrom(action.getPeriodsInterval());
        Date periodTo = PeriodConvertor.getDateTo(action.getPeriodsInterval());
        SearchOrderingFilter filter = new SearchOrderingFilter();
        filter.setSearchOrdering(action.getOrdering());
        filter.setAscSorting(action.isAscSorting());
        if(!action.isDeclaration()){
            List<DepartmentFormType> departmentFormTypes;
            if (action.getMode() == SourceMode.SOURCES) {
                departmentFormTypes = sourceService.
                        getDFTSourcesByDFT(action.getDepartmentId(), action.getTypeId(), action.getKind(), periodFrom,
                                periodTo, filter);
            } else {
                departmentFormTypes = sourceService.
                        getFormDestinations(action.getDepartmentId(), action.getTypeId(), action.getKind(), periodFrom, periodTo);
            }
            result.setCurrentSources(sourceAssembler.assembleDFT(departmentFormTypes));
        } else {
            if (action.getMode() == SourceMode.SOURCES) {
                List<DepartmentFormType> departmentFormTypes = sourceService
                        .getDFTSourceByDDT(action.getDepartmentId(), action.getTypeId(), periodFrom, periodTo, filter);
                result.setCurrentSources(sourceAssembler.assembleDFT(departmentFormTypes));
            } else {
                List<DepartmentDeclarationType> departmentFormTypes = sourceService.
                        getDeclarationDestinations(action.getDepartmentId(), action.getTypeId(), action.getKind(), periodFrom, periodTo);
                result.setCurrentSources(sourceAssembler.assembleDDT(departmentFormTypes));
            }
        }

        return result;
    }

    @Override
    public void undo(GetCurrentAssignsAction arg0, GetCurrentAssignsResult arg1, ExecutionContext arg2)
            throws ActionException {
        // Nothing!
    }
}
