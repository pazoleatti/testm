package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationDepartmentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDeclarationDepartmentsHandler extends AbstractActionHandler<GetDeclarationDepartmentsAction, GetDeclarationDepartmentsResult> {

    public GetDeclarationDepartmentsHandler() {
        super(GetDeclarationDepartmentsAction.class);
    }

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public GetDeclarationDepartmentsResult execute(GetDeclarationDepartmentsAction action, ExecutionContext context) throws ActionException {
        GetDeclarationDepartmentsResult result = new GetDeclarationDepartmentsResult();

        // Доступные подразделения
        List<Integer> departments =
                departmentService.getOpenPeriodDepartments(securityService.currentUserInfo().getUser(), asList(action.getTaxType()), action.getReportPeriodId());
        if (departments.isEmpty()){
            result.setDepartments(new ArrayList<Department>());
            result.setDepartmentIds(new HashSet<Integer>());
        } else {
            Set<Integer> departmentIds = new HashSet<Integer>(departments);
            result.setDepartments(new ArrayList<Department>(
                    departmentService.getRequiredForTreeDepartments(departmentIds).values()));
            result.setDepartmentIds(departmentIds);
        }
        return result;
    }

    @Override
    public void undo(GetDeclarationDepartmentsAction action, GetDeclarationDepartmentsResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
