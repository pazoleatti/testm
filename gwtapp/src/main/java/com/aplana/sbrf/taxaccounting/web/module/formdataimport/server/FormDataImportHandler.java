package com.aplana.sbrf.taxaccounting.web.module.formdataimport.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdataimport.shared.FormDataImportAction;
import com.aplana.sbrf.taxaccounting.web.module.formdataimport.shared.FormDataImportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * 
 * @author Eugene Stetsenko Обработчик запроса для перехода между этапами.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class FormDataImportHandler extends AbstractActionHandler<FormDataImportAction, FormDataImportResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	public FormDataImportHandler() {
		super(FormDataImportAction.class);
	}

	@Override
	public FormDataImportResult execute(FormDataImportAction action, ExecutionContext context)
			throws ActionException {

			Logger logger = new Logger();
			formDataService.importFormData(logger, securityService.currentUserInfo(), action.getFormTemplateId(), action.getDepartmentId(), action.getKind(), action.getReportPeriodId());
			FormDataImportResult result = new FormDataImportResult();
			result.setLogEntries(logger.getEntries());
			return result;

	}

	@Override
	public void undo(FormDataImportAction arg0, FormDataImportResult arg1,
			ExecutionContext arg2) throws ActionException {
		// Ничего не делаем
		
	}
}
