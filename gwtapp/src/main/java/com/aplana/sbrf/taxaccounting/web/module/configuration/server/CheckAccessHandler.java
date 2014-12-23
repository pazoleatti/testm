package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.UserAuthenticationToken;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckAccessAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckAccessResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CheckAccessHandler extends AbstractActionHandler<CheckAccessAction, CheckAccessResult> {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuditService auditService;

    public CheckAccessHandler() {
        super(CheckAccessAction.class);
    }

    @Override
    public CheckAccessResult execute(CheckAccessAction action, ExecutionContext context) throws ActionException {
        CheckAccessResult result = new CheckAccessResult();
        Logger logger = new Logger();
        String uuid = null;
        ConfigurationParamModel model = action.getModel();
        ConfigurationParamGroup group = action.getGroup();

        if (group.equals(ConfigurationParamGroup.COMMON) || group.equals(ConfigurationParamGroup.FORM)) {
            configurationService.checkReadWriteAccess(securityService.currentUserInfo(), model, logger);
        } else if (group.equals(ConfigurationParamGroup.EMAIL)) {
            boolean success = emailService.testAuth(logger);
            if (!success) {
                UserAuthenticationToken principal = ((UserAuthenticationToken) (SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()));
                auditService.add(FormDataEvent.SEND_EMAIL, principal.getUserInfo(), 0, null, null, null, null,
                        logger.getEntries().get(0).getMessage(), uuid, null);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(CheckAccessAction action, CheckAccessResult result, ExecutionContext context) throws ActionException {
    }
}
