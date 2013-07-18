package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
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

        DepartmentCombined depCombined = new DepartmentCombined(
                departmentService.getDepartmentParam(action.getDepartmentId()),
                departmentService.getDepartmentParamIncome(action.getDepartmentId()),
                departmentService.getDepartmentParamTransport(action.getDepartmentId()));

        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);

        return result;
    }

    @Override
    public void undo(GetDepartmentCombinedAction action, GetDepartmentCombinedResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
