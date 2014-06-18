package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTemplateExt;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFormHandler extends AbstractActionHandler<GetFormAction, GetFormResult> {
    @Autowired
	private FormTemplateService formTemplateService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private LogEntryService logEntryService;

    public GetFormHandler() {
        super(GetFormAction.class);
    }

    @Override
    public GetFormResult execute(GetFormAction action, ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();

        Logger logger = new Logger();
        GetFormResult result = new GetFormResult();
        try {
            formTemplateService.checkLockedByAnotherUser(action.getId(), userInfo);
        } catch (AccessDeniedException e) {
            logger.error(e);
            result.setLockedByAnotherUser(true);
        }
        if (!result.isLockedByAnotherUser()) {
            formTemplateService.lock(action.getId(), userInfo);
        }
        FormTemplateExt formTemplateExt = new FormTemplateExt();
		FormTemplate formTemplate = formTemplateService.getFullFormTemplate(action.getId(), logger);
        formTemplateExt.setActualEndVersionDate(formTemplateService.getFTEndDate(formTemplate.getId()));
        formTemplate.setScript(formTemplateService.getFormTemplateScript(action.getId(), logger));
        formTemplateExt.setFormTemplate(formTemplate);
        result.setForm(formTemplateExt);
        result.setRefBookList(refBookFactory.getAll(false));

        if (!logger.getEntries().isEmpty())
            result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(GetFormAction action, GetFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
