package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CreateIfrsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CreateIfrsDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class CreateIfrsDataHandler extends AbstractActionHandler<CreateIfrsDataAction, CreateIfrsDataResult> {

    @Autowired
    private IfrsDataService ifrsDataService;
    @Autowired
    SecurityService securityService;

    public CreateIfrsDataHandler() {
        super(CreateIfrsDataAction.class);
    }

    @Override
    public CreateIfrsDataResult execute(CreateIfrsDataAction action, ExecutionContext executionContext) throws ActionException {
        CreateIfrsDataResult result = new CreateIfrsDataResult();
        ifrsDataService.create(action.getReportPeriodId());
        return result;
    }

    @Override
    public void undo(CreateIfrsDataAction action, CreateIfrsDataResult result, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
