package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBSOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBSOpenDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetBSOpenDataHandler extends AbstractActionHandler<GetBSOpenDataAction, GetBSOpenDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    SourceService departmentFormTypService;

    public GetBSOpenDataHandler() {
        super(GetBSOpenDataAction.class);
    }

    @Override
    public GetBSOpenDataResult execute(GetBSOpenDataAction action, ExecutionContext executionContext) throws ActionException {
        GetBSOpenDataResult result = new GetBSOpenDataResult();

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

        // Подразделения доступные пользователю
        Set<Integer> avSet = new HashSet<Integer>();
        if (!currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Первичные и консолид. отчеты для налога на прибыль
            List<DepartmentFormType> formIncomeSrcList = departmentFormTypService.getDFTSourcesByDepartment(currUser.getDepartmentId(), TaxType.INCOME);

            for (DepartmentFormType ft : formIncomeSrcList) {
                avSet.add(ft.getDepartmentId());
            }

            // Подразделение пользователя
            avSet.add(currUser.getDepartmentId());

            // Необходимые для дерева подразделения
            result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));

        } else {
            // Все подразделения
            result.setDepartments(departmentService.listAll());

            for (Department dep : result.getDepartments()) {
                avSet.add(dep.getId());
            }
        }
        result.setAvailableDepartments(avSet);

        // Подразделение текущего пользователя
        result.setDepartment(departmentService.getDepartment(currUser.getDepartmentId()));

        return result;
    }

    @Override
    public void undo(GetBSOpenDataAction action, GetBSOpenDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
