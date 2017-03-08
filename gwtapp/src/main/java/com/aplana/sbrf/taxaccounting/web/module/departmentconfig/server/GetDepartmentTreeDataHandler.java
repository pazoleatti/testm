package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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

import static java.util.Arrays.asList;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDepartmentTreeDataHandler extends AbstractActionHandler<GetDepartmentTreeDataAction, GetDepartmentTreeDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PeriodService periodService;

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

        if (!action.isOnlyPeriods()) {
            if (!currUser.hasRoles(action.getTaxType(), TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS,
                    TARole.F_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_NS)) {
                // Не контролер, далее не загружаем
                return result;
            }

            // Подразделения доступные пользователю
            Set<Integer> avSet = new HashSet<Integer>();
            if (currUser.hasRoles(action.getTaxType(), TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
                // Все подразделения
                result.setDepartments(departmentService.listAll());

                for (Department dep : result.getDepartments()) {
                    avSet.add(dep.getId());
                }
            } else {
                // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
                avSet.addAll(departmentService.getTaxFormDepartments(currUser, action.getTaxType(), null, null));

                // Необходимые для дерева подразделения
                result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));
            }

            result.setAvailableDepartments(avSet);
            result.setReportPeriods(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(), asList(currUser.getDepartmentId())));
        } else {
            result.setReportPeriods(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(), asList(action.getDepartmentId())));
        }

        return result;
    }

    @Override
    public void undo(GetDepartmentTreeDataAction action, GetDepartmentTreeDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
