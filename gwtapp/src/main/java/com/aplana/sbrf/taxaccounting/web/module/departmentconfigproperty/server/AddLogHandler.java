package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.AddLogAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.AddLogResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class AddLogHandler extends AbstractActionHandler<AddLogAction, AddLogResult> {

    public AddLogHandler() {
        super(AddLogAction.class);
    }

    @Autowired
    LogEntryService logService;

    @Override
    public AddLogResult execute(AddLogAction addLogAction, ExecutionContext executionContext) throws ActionException {
        AddLogResult result = new AddLogResult();
        result.setUuid(logService.update(addLogAction.getMessages(), addLogAction.getOldUUID()));
        return result;
    }

    @Override
    public void undo(AddLogAction addLogAction, AddLogResult addLogResult, ExecutionContext executionContext) throws ActionException {

    }
}
