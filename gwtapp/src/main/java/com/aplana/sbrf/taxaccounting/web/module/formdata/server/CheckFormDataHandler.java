package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DataRowService;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Eugene Stetsenko Обработчик запроса для проверки формы.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class CheckFormDataHandler extends
		AbstractActionHandler<CheckFormDataAction, DataRowResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DataRowService dataRowService;

    @Autowired
    private LogEntryService logEntryService;

	public CheckFormDataHandler() {
		super(CheckFormDataAction.class);
	}

	@Override
	public DataRowResult execute(CheckFormDataAction action,
			ExecutionContext context) throws ActionException {

		Logger logger = new Logger();
		FormData formData = action.getFormData();
		TAUserInfo userInfo = securityService.currentUserInfo();
		if (!action.getModifiedRows().isEmpty()) {
			dataRowService.update(userInfo, formData.getId(), action.getModifiedRows());
		}
		formDataService.doCheck(logger, securityService.currentUserInfo(), formData);
		DataRowResult result = new DataRowResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
	}

	@Override
	public void undo(CheckFormDataAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
