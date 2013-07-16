package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDeclarationCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDeclarationCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationCombinedHandler extends AbstractActionHandler<GetDeclarationCombinedAction, GetDeclarationCombinedResult> {

    @Autowired
    private DepartmentService departmentService;

    public GetDeclarationCombinedHandler() {
        super(GetDeclarationCombinedAction.class);
    }

    @Override
    public GetDeclarationCombinedResult execute(GetDeclarationCombinedAction action, ExecutionContext executionContext) throws ActionException {
        Department dep = departmentService.getDepartment(action.getDepartmentId());

        if (dep == null) {
            return null;
        }

        DepartmentCombined depCombined = new DepartmentCombined(dep);

        if (dep != null) {
            depCombined.setCommonParams(departmentService.getDepartmentParam(action.getDepartmentId()));
            depCombined.setIncomeParams(departmentService.getDepartmentParamIncome(action.getDepartmentId()));
            depCombined.setTransportParams(departmentService.getDepartmentParamTransport(action.getDepartmentId()));
        }

        GetDeclarationCombinedResult result = new GetDeclarationCombinedResult();
        result.setDepartmentCombined(depCombined);

        return result;
    }

    @Override
    public void undo(GetDeclarationCombinedAction formListAction, GetDeclarationCombinedResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
