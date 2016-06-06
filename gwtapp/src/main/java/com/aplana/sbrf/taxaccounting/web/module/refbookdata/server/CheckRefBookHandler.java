package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("isAuthenticated()")
public class CheckRefBookHandler extends AbstractActionHandler<CheckRefBookAction, CheckRefBookResult> {

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private SecurityService securityService;

    public CheckRefBookHandler() {
        super(CheckRefBookAction.class);
    }

    @Override
    public CheckRefBookResult execute(CheckRefBookAction action, ExecutionContext context) throws ActionException {
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        CheckRefBookResult result = new CheckRefBookResult();
        result.setAvailable(refBook.isVisible());
        result.setVersioned(refBook.isVersioned());
        result.setUploadAvailable(securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP));
        result.setScriptStatus(refBookFactory.getEventScriptStatus(action.getRefBookId(), FormDataEvent.IMPORT));
        return result;
    }

    @Override
    public void undo(CheckRefBookAction action, CheckRefBookResult result, ExecutionContext context) throws ActionException {

    }
}
