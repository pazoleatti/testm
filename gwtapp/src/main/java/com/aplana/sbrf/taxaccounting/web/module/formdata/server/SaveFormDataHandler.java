package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ExtActionException;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class SaveFormDataHandler extends AbstractActionHandler<SaveFormDataAction, FormDataResult> {
    private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private SecurityService securityService;
	@Autowired
	private FormDataService formDataService;

    public SaveFormDataHandler() {
        super(SaveFormDataAction.class);
    }

    @Override
    public FormDataResult execute(SaveFormDataAction action, ExecutionContext context) throws ActionException {
        try {
            Logger logger = new Logger();
            FormData formData = action.getFormData();
			TAUser currentUser = securityService.currentUser();
			formDataService.saveFormData(logger, currentUser.getId(), formData);
			logger.info("Данные успешно записаны");
			FormDataResult result = new FormDataResult();
			result.setFormData(formData);
            result.setLogEntries(logger.getEntries());
            return result;
        } catch (ServiceLoggerException e) {
        	throw new ExtActionException(e.getLocalizedMessage(), e.getLogEntries());
        } catch (Exception e) {
            log.error("Failed to save FormData object", e);
            throw new ExtActionException(e);
        }
    }

    @Override
    public void undo(SaveFormDataAction action, FormDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
