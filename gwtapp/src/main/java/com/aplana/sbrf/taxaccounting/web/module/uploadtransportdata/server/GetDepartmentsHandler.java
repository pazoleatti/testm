package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.shared.GetDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.shared.GetDepartmentsResult;
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
@Service("UploadGetDepartmentsHandler")
@PreAuthorize("hasAnyRole('ROLE_OPER','ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDepartmentsHandler extends AbstractActionHandler<GetDepartmentsAction, GetDepartmentsResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    public GetDepartmentsHandler() {
        super(GetDepartmentsAction.class);
    }

    @Override
    public GetDepartmentsResult execute(GetDepartmentsAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetDepartmentsResult result = new GetDepartmentsResult();
        Set<Integer> availableDepartments = new HashSet<Integer>();

        // 20 - Получение ТБ
        List<Department> departmentList = departmentService.getTBDepartments(userInfo.getUser());
        // Подразделение по-умолчанию
        if (userInfo.getUser().hasRole("ROLE_CONTROL_UNP")) {
            result.setCanChooseDepartment(true);
            List<Department> unpList = departmentService.getTBUserDepartments(userInfo.getUser());
            if (unpList != null && !unpList.isEmpty()) {
                result.setDefaultDepartmentId(unpList.get(0).getId());
            }
        } else {
            if (departmentList != null && !departmentList.isEmpty()) {
                result.setDefaultDepartmentId(departmentList.get(0).getId());
            }
        }

        if (departmentList != null) {
            for (Department department : departmentList) {
                availableDepartments.add(department.getId());
            }
        }
        result.setAvailableDepartments(availableDepartments);

        // Подразделения необходимые для построения дерева
        if (!availableDepartments.isEmpty()) {
            Map<Integer, Department> required = departmentService.getRequiredForTreeDepartments(availableDepartments);
            result.setDepartments(new ArrayList<Department>(required.values()));
        } else {
            result.setDepartments(new ArrayList<Department>(0));
        }
        return result;
    }

    @Override
    public void undo(GetDepartmentsAction action, GetDepartmentsResult result, ExecutionContext context) throws ActionException {
        // Не требуется
    }
}
