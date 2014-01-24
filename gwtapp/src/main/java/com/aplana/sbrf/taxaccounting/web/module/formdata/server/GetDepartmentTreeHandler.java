package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetDepartmentTreeAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetDepartmentTreeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GetDepartmentTreeHandler extends AbstractActionHandler<GetDepartmentTreeAction, GetDepartmentTreeResult> {

    @Autowired
    DepartmentService departmentService;

    public GetDepartmentTreeHandler() {
        super(GetDepartmentTreeAction.class);
    }

    @Override
    public GetDepartmentTreeResult execute(GetDepartmentTreeAction action, ExecutionContext executionContext) throws ActionException {
        GetDepartmentTreeResult result = new GetDepartmentTreeResult();
        Set<Integer> avSet = new HashSet<Integer>();
        avSet.addAll(departmentService.getPrintFormDepartments(action.getFormData()));
        result.setAvailableDepartments(avSet);
        result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));
        return result;
    }

    @Override
    public void undo(GetDepartmentTreeAction getDepartmentTreeAction, GetDepartmentTreeResult getDepartmentTreeResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
