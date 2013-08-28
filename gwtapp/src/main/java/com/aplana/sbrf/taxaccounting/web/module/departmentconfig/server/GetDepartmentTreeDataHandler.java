package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentTreeDataAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentTreeDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDepartmentTreeDataHandler extends AbstractActionHandler<GetDepartmentTreeDataAction, GetDepartmentTreeDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    SourceService departmentFormTypService;

    public GetDepartmentTreeDataHandler() {
        super(GetDepartmentTreeDataAction.class);
    }

    @Override
    public GetDepartmentTreeDataResult execute(GetDepartmentTreeDataAction action, ExecutionContext executionContext) throws ActionException {
        GetDepartmentTreeDataResult result = new GetDepartmentTreeDataResult();

        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        if (!currUser.hasRole(TARole.ROLE_CONTROL_UNP) && !currUser.hasRole(TARole.ROLE_CONTROL)) {
            // Не контролер, далее не загружаем
            return result;
        }
        if (!Arrays.asList(TaxType.INCOME, TaxType.TRANSPORT, TaxType.DEAL).contains(action.getTaxType())) {
            // Не соответствующий тип налога, далее не загружаем
            return result;
        }

        // Подразделения доступные пользователю
        Set<Integer> avSet = new HashSet<Integer>();
        if (!currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Первичные и консолид. отчеты
            List<DepartmentFormType> formSrcList =  departmentFormTypService.getDFTSourcesByDepartment(currUser.getDepartmentId(), action.getTaxType());

            for (DepartmentFormType ft : formSrcList) {
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

        return result;
    }

    @Override
    public void undo(GetDepartmentTreeDataAction action, GetDepartmentTreeDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
