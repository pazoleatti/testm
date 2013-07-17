package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetOpenDataResult;
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
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetOpenDataHandler extends AbstractActionHandler<GetOpenDataAction, GetOpenDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    DepartmentFormTypeService departmentFormTypService;

    @Autowired
    private RefBookDao rbDao;

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

        // Подразделения доступные пользователю
        Set<Integer> avSet = new HashSet<Integer>();
        if (!currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Первичные и консолид. отчеты
            List<DepartmentFormType> formIncomeSrcList = departmentFormTypService.getDepartmentFormSources(currUser.getDepartmentId(), TaxType.INCOME);
            List<DepartmentFormType> formTransportSrcList = departmentFormTypService.getDepartmentFormSources(currUser.getDepartmentId(), TaxType.TRANSPORT);

            for (DepartmentFormType ft : formIncomeSrcList) {
                avSet.add(ft.getDepartmentId());
            }

            for (DepartmentFormType ft : formTransportSrcList) {
                avSet.add(ft.getDepartmentId());
            }

            // Подразделение пользователя
            avSet.add(currUser.getDepartmentId());

            // Необходимые для дерева подразделения
            result.setDepartments(new ArrayList(departmentService.getRequiredForTreeDepartments(avSet).values()));

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
    public void undo(GetOpenDataAction formListAction, GetOpenDataResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
