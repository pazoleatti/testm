package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetDepartmentAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetDepartmentResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDepartmentHandler extends AbstractActionHandler<GetDepartmentAction, GetDepartmentResult> {

    public GetDepartmentHandler() {
        super(GetDepartmentAction.class);
    }

    @Autowired
    DepartmentService departmentService;

    @Override
    public GetDepartmentResult execute(GetDepartmentAction getDepartmentAction, ExecutionContext executionContext) throws ActionException {
        GetDepartmentResult result = new GetDepartmentResult();
        result.setDepartment(departmentService.getDepartment(getDepartmentAction.getDepartmentId()));
        return result;
    }

    @Override
    public void undo(GetDepartmentAction getDepartmentAction, GetDepartmentResult getDepartmentResult, ExecutionContext executionContext) throws ActionException {

    }
}
