package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetCheckDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetCheckDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static java.util.Arrays.asList;

/**
 * @author Lenar Haziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCheckDeclarationHandler extends AbstractActionHandler<GetCheckDeclarationAction, GetCheckDeclarationResult> {

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    SourceService departmentFormTypService;

    public GetCheckDeclarationHandler() {
        super(GetCheckDeclarationAction.class);
    }

    @Override
    public GetCheckDeclarationResult execute(GetCheckDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        GetCheckDeclarationResult result = new GetCheckDeclarationResult();
        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        declarationDataFilter.setReportPeriodIds(asList(action.getReportPeriodId()));
        declarationDataFilter.setDepartmentIds(asList(action.getDepartment()));
        declarationDataFilter.setSearchOrdering(DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME);
        declarationDataFilter.setStartIndex(0);
        declarationDataFilter.setCountOfRecords(10);
        PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(declarationDataFilter);
        String text = "";
        for(DeclarationDataSearchResultItem item: page) {
            if (text != "") text += ", ";
            text += item.getDeclarationType();
        }
        result.setDeclarationTypes(text);
        return result;
    }

    @Override
    public void undo(GetCheckDeclarationAction action, GetCheckDeclarationResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}