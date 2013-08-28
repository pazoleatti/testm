package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetOpenDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
@Component("getNominationOpenDataHandler")
public class GetOpenDataHandler extends AbstractActionHandler<GetOpenDataAction, GetOpenDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    SourceService departmentFormTypService;

    public GetOpenDataHandler() {
        super(GetOpenDataAction.class);
    }

    @Override
    public GetOpenDataResult execute(GetOpenDataAction action, ExecutionContext executionContext) throws ActionException {
        GetOpenDataResult result = new GetOpenDataResult();

        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        // TODO вопрос в аналитике (УВиСАС), нужно ли сюда добавить администратора

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

            // Подразделение пользователя и все дочерние
            for (Department dep : departmentService.getAllChildren(currUser.getDepartmentId())) {
                avSet.add(dep.getId());
            }

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

        return result;
    }

    @Override
    public void undo(GetOpenDataAction action, GetOpenDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
