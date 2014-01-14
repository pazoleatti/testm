package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetOpenDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Component("getNominationOpenDataHandler")
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
        } else if (currUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            result.setControlUNP(false);
        }

        if (result.getControlUNP() == null) {
            // Не контролер, далее не загружаем
            return result;
        }

        // Подразделения доступные пользователю
        Set<Integer> avSet = new HashSet<Integer>();

        // http://conf.aplana.com/pages/viewpage.action?pageId=11380675
        if (currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Все подразделения
            result.setDepartments(departmentService.listAll());

            for (Department dep : result.getDepartments()) {
                avSet.add(dep.getId());
            }
        } else if (currUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            for (Department dep : departmentService.getBADepartments(currUser)) {
                avSet.add(dep.getId());
            }
            // Необходимые для дерева подразделения
            result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));
        }
        result.setAvailableDepartments(avSet);

        return result;
    }

    @Override
    public void undo(GetOpenDataAction action, GetOpenDataResult result, ExecutionContext executionContext)
            throws ActionException {
        // Не требуется
    }
}
