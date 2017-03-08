package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetUserDepartmentAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetUserDepartmentResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetUserDepartmentHandler extends AbstractActionHandler<GetUserDepartmentAction, GetUserDepartmentResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

    public GetUserDepartmentHandler() {
        super(GetUserDepartmentAction.class);
    }

    @Override
    public GetUserDepartmentResult execute(GetUserDepartmentAction action, ExecutionContext context) throws ActionException {
        GetUserDepartmentResult result = new GetUserDepartmentResult();

        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        // Подразделение текущего пользователя
        result.setDepartment(departmentService.getDepartment(currUser.getDepartmentId()));

        return result;
    }

    @Override
    public void undo(GetUserDepartmentAction action, GetUserDepartmentResult result, ExecutionContext context) throws ActionException {
        // Не требуется
    }
}
