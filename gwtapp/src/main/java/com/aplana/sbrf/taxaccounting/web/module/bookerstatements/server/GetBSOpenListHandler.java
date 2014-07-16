package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBSOpenListAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBSOpenListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetBSOpenListHandler extends AbstractActionHandler<GetBSOpenListAction, GetBSOpenListResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    SourceService departmentFormTypService;

    @Autowired
    private PeriodService periodService;

    public GetBSOpenListHandler() {
        super(GetBSOpenListAction.class);
    }

    @Override
    public GetBSOpenListResult execute(GetBSOpenListAction action, ExecutionContext executionContext) throws ActionException {
        GetBSOpenListResult result = new GetBSOpenListResult();

        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        // Все отчетные периоды
        result.setReportPeriods(periodService.getAllPeriodsByTaxType(TaxType.INCOME, true));

        // Признак контролера
        if (currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            result.setControlUNP(true);
        } else if (currUser.hasRole(TARole.ROLE_CONTROL) || currUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            result.setControlUNP(false);
        }

        if (result.getControlUNP() == null) {
            // Не контролер, далее не загружаем
            return result;
        }

        // Подразделения доступные пользователю
        Set<Integer> avSet = new HashSet<Integer>();

        if (currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Все подразделения
            result.setDepartments(departmentService.listAll());
            for (Department dep : result.getDepartments()) {
                avSet.add(dep.getId());
            }
        } else {
            //TODO в 039 этого уже нет. Убрать после мержа
            avSet.addAll(departmentService.getTaxFormDepartments(currUser, asList(TaxType.INCOME), null, null));

            // Необходимые для дерева подразделения
            result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));
        }
        result.setAvailableDepartments(avSet);

        // Подразделение текущего пользователя
        result.setDepartment(departmentService.getDepartment(currUser.getDepartmentId()));
        result.setBookerReportTypes(Arrays.asList(BookerStatementsType.values()));

        return result;
    }

    @Override
    public void undo(GetBSOpenListAction action, GetBSOpenListResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
