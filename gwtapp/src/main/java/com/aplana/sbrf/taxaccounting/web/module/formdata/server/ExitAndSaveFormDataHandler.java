package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ExitAndSaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class ExitAndSaveFormDataHandler extends
        AbstractActionHandler<ExitAndSaveFormDataAction, DataRowResult> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private FormDataService formDataService;
	@Autowired
	private DataRowService dataRowService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
	private RefBookHelper refBookHelper;

	public ExitAndSaveFormDataHandler() {
		super(ExitAndSaveFormDataAction.class);
	}

	@Override
	public DataRowResult execute(ExitAndSaveFormDataAction action, ExecutionContext context) throws ActionException {
		Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
		FormData formData = action.getFormData();
        if (!action.getModifiedRows().isEmpty()) {
            refBookHelper.dataRowsCheck(action.getModifiedRows(), formData.getFormColumns());
            dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());
        }
        formDataService.saveFormData(logger, securityService.currentUserInfo(), formData);
        formDataService.unlock(formData.getId(), userInfo);
        dataRowService.removeCheckPoint(formData);
		DataRowResult result = new DataRowResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(ExitAndSaveFormDataAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
