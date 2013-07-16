package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedResult;
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
public class GetDepartmentCombinedHandler extends AbstractActionHandler<GetDepartmentCombinedAction, GetDepartmentCombinedResult> {

    @Autowired
    private DepartmentService departmentService;

    public GetDepartmentCombinedHandler() {
        super(GetDepartmentCombinedAction.class);
    }

    @Override
    public GetDepartmentCombinedResult execute(GetDepartmentCombinedAction action, ExecutionContext executionContext) throws ActionException {
        Department dep = departmentService.getDepartment(action.getDepartmentId());

        if (dep == null) {
            return null;
        }

        DepartmentCombined depCombined = new DepartmentCombined(dep,
                departmentService.getDepartmentParam(action.getDepartmentId()),
                departmentService.getDepartmentParamIncome(action.getDepartmentId()),
                departmentService.getDepartmentParamTransport(action.getDepartmentId()));

        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);

        return result;
    }

    @Override
    public void undo(GetDepartmentCombinedAction formListAction, GetDepartmentCombinedResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
