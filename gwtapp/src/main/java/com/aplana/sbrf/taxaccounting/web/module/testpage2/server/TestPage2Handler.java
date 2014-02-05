package com.aplana.sbrf.taxaccounting.web.module.testpage2.server;

import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.testpage2.shared.GetDataAction;
import com.aplana.sbrf.taxaccounting.web.module.testpage2.shared.GetDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS', 'ROLE_CONF')")
public class TestPage2Handler extends AbstractActionHandler<GetDataAction, GetDataResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    TAUserService taUserService;

    public TestPage2Handler() {
        super(GetDataAction.class);
    }

    @Override
    public GetDataResult execute(GetDataAction getDataAction, ExecutionContext executionContext) throws ActionException {
        System.out.println("====== getDataAction.id = " + getDataAction.getId()); // TODO (Ramil Timerbaev)
        GetDataResult result = new GetDataResult();
        result.setValues(taUserService.listAllUsers());
        return result;
    }

    @Override
    public void undo(GetDataAction getDataAction, GetDataResult getDataResult, ExecutionContext executionContext) throws ActionException {

    }
}
