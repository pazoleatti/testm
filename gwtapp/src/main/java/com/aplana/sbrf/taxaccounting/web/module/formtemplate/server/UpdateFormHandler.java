package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.TemplateChangesEvent;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.UpdateFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class UpdateFormHandler extends AbstractActionHandler<UpdateFormAction, UpdateFormResult> {

	@Autowired
	private FormTemplateService formTemplateService;

    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private TemplateChangesService templateChangesService;

    @Autowired
    private SecurityService securityService;

    public UpdateFormHandler() {
        super(UpdateFormAction.class);
    }

    @Override
    public UpdateFormResult execute(UpdateFormAction action, ExecutionContext context) {
		Logger logger = new Logger();
		UpdateFormResult result = new UpdateFormResult();

        int formTemplateId = 0;
        TemplateChanges changes = new TemplateChanges();

		formTemplateService.validateFormTemplate(action.getForm(), logger);
		if (logger.getEntries().isEmpty() && action.getForm().getId() != null && action.getForm().getType().getId() != 0) {
			/*formTemplateService.save(action.getForm());*/
            formTemplateId = mainOperatingService.edit(action.getForm().getId(), action.getVersionEndDate(), logger);
            changes.setEvent(TemplateChangesEvent.MODIFIED);
            changes.setEventDate(new Date());
            changes.setFormTemplateId(formTemplateId);
            changes.setAuthor(securityService.currentUserInfo().getUser());
            templateChangesService.save(changes);
		} else if(logger.getEntries().isEmpty() && action.getForm().getId() == null && action.getForm().getType().getId() != 0){
            formTemplateId = mainOperatingService.createNewTemplateVersion(action.getForm(), action.getVersionEndDate(), logger);
            changes.setEvent(TemplateChangesEvent.CREATED);
            changes.setEventDate(new Date());
            changes.setFormTemplateId(formTemplateId);
            changes.setAuthor(securityService.currentUserInfo().getUser());
            templateChangesService.save(changes);
        } else if(logger.getEntries().isEmpty() && action.getForm().getId() == null && action.getForm().getType().getId() == 0){
            formTemplateId = mainOperatingService.createNewFormType(action.getForm(), action.getVersionEndDate(), logger);
            changes.setEvent(TemplateChangesEvent.CREATED);
            changes.setEventDate(new Date());
            changes.setFormTemplateId(formTemplateId);
            changes.setAuthor(securityService.currentUserInfo().getUser());
            templateChangesService.save(changes);
        }

		result.setLogEntries(logger.getEntries());
        result.setFormTemplateId(formTemplateId);
		return result;
    }

    @Override
    public void undo(UpdateFormAction action, UpdateFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
