package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDeclarationCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDeclarationCombinedResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetOpenDataResult;
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
public class GetOpenDataHandler extends AbstractActionHandler<GetOpenDataAction, GetOpenDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    public GetOpenDataHandler() {
        super(GetOpenDataAction.class);
    }

    @Override
    public GetOpenDataResult execute(GetOpenDataAction action, ExecutionContext executionContext) throws ActionException {
        GetOpenDataResult result = new GetOpenDataResult();

        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        // Признак контролера
        if (currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            result.setControlUNP(true);
        } else if (currUser.hasRole(TARole.ROLE_CONTROL)) {
            result.setControlUNP(false);
        }

        if (result.getControlUNP() == null) {
            // Не контролер, далее не загружаем
            return result;
        }

        // Подразделения
        if (currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            result.setDepartments(departmentService.listAll());
        } else {
            // TODO (Dmitriy Levykin) Грузить только доступные пользователю подразделения
            // См. GetFormDataListHandler
            result.setDepartments(departmentService.listDepartments());
        }

        // Подразделение текущего пользователя
        result.setDepartment(departmentService.getDepartment(currUser.getDepartmentId()));

        return result;
    }

    @Override
    public void undo(GetOpenDataAction formListAction, GetOpenDataResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
