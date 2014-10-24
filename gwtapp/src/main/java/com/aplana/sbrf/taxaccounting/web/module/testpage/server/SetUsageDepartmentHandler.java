package com.aplana.sbrf.taxaccounting.web.module.testpage.server;

import com.aplana.sbrf.taxaccounting.common.service.CommonServiceException;
import com.aplana.sbrf.taxaccounting.common.service.DepartmentUsageService;
import com.aplana.sbrf.taxaccounting.web.module.testpage.shared.SetUsageDepartmentAction;
import com.aplana.sbrf.taxaccounting.web.module.testpage.shared.SetUsageDepartmentResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author aivanov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SetUsageDepartmentHandler extends AbstractActionHandler<SetUsageDepartmentAction, SetUsageDepartmentResult> {

    @Autowired
    DepartmentUsageService departmentUsageService;


    public SetUsageDepartmentHandler() {
        super(SetUsageDepartmentAction.class);
    }

    @Override
    public SetUsageDepartmentResult execute(SetUsageDepartmentAction action, ExecutionContext executionContext) throws ActionException {
        SetUsageDepartmentResult result = new SetUsageDepartmentResult();

        try {
            departmentUsageService.setDepartmentUsedByGarant(action.getDepartmentId(), action.isUsed());
        } catch (CommonServiceException e) {
            result.setMessage("Ошибка: " + e.getMessage());
            return result;
        }

        result.setMessage("Успех");
        return result;
    }


    @Override
    public void undo(SetUsageDepartmentAction deleteFormsSourseAction, SetUsageDepartmentResult deleteFormsSourceResult, ExecutionContext executionContext) throws ActionException {

    }
}
