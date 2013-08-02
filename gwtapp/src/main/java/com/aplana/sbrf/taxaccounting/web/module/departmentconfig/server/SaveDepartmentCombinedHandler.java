package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class SaveDepartmentCombinedHandler extends AbstractActionHandler<SaveDepartmentCombinedAction, SaveDepartmentCombinedResult> {

    public SaveDepartmentCombinedHandler() {
        super(SaveDepartmentCombinedAction.class);
    }

    @Override
    public SaveDepartmentCombinedResult execute(SaveDepartmentCombinedAction action, ExecutionContext executionContext) throws ActionException {

        DepartmentCombined departmentCombined = action.getDepartmentCombined();
        // TODO Заменить на рефбуки
        return new SaveDepartmentCombinedResult();
    }

    @Override
    public void undo(SaveDepartmentCombinedAction action, SaveDepartmentCombinedResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
