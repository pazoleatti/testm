package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDestanationPopupDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDestanationPopupDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author auldanov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
class GetDestanationPopupDataHandler extends AbstractActionHandler<GetDestanationPopupDataAction, GetDestanationPopupDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SourceService sourceService;


    public GetDestanationPopupDataHandler() {
        super(GetDestanationPopupDataAction.class);
    }

    @Override
    public GetDestanationPopupDataResult execute(GetDestanationPopupDataAction action, ExecutionContext executionContext) throws ActionException {
        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        // модель для результата метода
        GetDestanationPopupDataResult result = new GetDestanationPopupDataResult();

        // Подразделения доступные пользователю
        Set<Integer> availableDepartmentSet = new HashSet<Integer>();
        List<Department> availableDepartmentList = departmentService.getBADepartments(currUser);
        for (Department d: availableDepartmentList){
            availableDepartmentSet.add(d.getId());
        }
        result.setAvailableDepartments(availableDepartmentSet);

        // Все подразделения
        result.setDepartments(departmentService.listAll());

        // Исполнители доступные пользователю
        Set<Integer> availablePerformersSet = new HashSet<Integer>();
        List<Department> availablePerformersList = departmentService.getDestinationDepartments(currUser);
        for (Department d: availablePerformersList){
            availablePerformersSet.add(d.getId());
        }
        result.setAvailablePerformers(availablePerformersSet);

        // Все подразделения
        result.setPerformers(departmentService.listAll());

        return result;
    }

    @Override
    public void undo(GetDestanationPopupDataAction getDestanationPopupDataAction, GetDestanationPopupDataResult getDestanationPopupDataResult, ExecutionContext executionContext) throws ActionException {

    }
}
