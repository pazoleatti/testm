package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ConsolidateAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ConsolidateResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class ConsolidateHandler extends AbstractActionHandler<ConsolidateAction, ConsolidateResult> {

    @Autowired
    FormDataService formDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    LogEntryService logEntryService;

    public ConsolidateHandler() {
        super(ConsolidateAction.class);
    }

    @Override
    public ConsolidateResult execute(ConsolidateAction action, ExecutionContext executionContext) throws ActionException {
        ConsolidateResult result = new ConsolidateResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        FormData formData = formDataService.getFormData(
                userInfo,
                action.getFormDataId(),
                action.isManual(),
                logger);
        formDataService.compose(formData, userInfo, logger);
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(ConsolidateAction consolidateAction, ConsolidateResult consolidateResult, ExecutionContext executionContext) throws ActionException {

    }
}
