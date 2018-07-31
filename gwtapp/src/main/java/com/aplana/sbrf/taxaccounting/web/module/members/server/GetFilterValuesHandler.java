package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.FilterValues;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.GetFilterValues;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class GetFilterValuesHandler extends AbstractActionHandler<GetFilterValues, FilterValues> {

    @Autowired
    TARoleService taRoleService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    SecurityService securityService;
    @Autowired
    SourceService departmentFormTypService;

    public GetFilterValuesHandler() {
        super(GetFilterValues.class);
    }

    @Override
    public FilterValues execute(GetFilterValues action, ExecutionContext context) throws ActionException {
        FilterValues result = new FilterValues();

        TAUser currentUser = securityService.currentUserInfo().getUser();

        result.setCanEdit(false);

        List<TARole> allRoles = taRoleService.getAllSbrfRoles();

        result.setRoles(allRoles);

        Set<Integer> depIds = new HashSet<Integer>();

        if (currentUser.hasRole(TARole.ROLE_ADMIN)) {
            depIds.addAll(departmentService.listIdAll());
        } else {
            depIds.addAll(departmentService.getBADepartmentIds(currentUser));
        }
        result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(depIds).values()));

        return result;
    }

    @Override
    public void undo(GetFilterValues action, FilterValues result, ExecutionContext context) throws ActionException {

    }
}