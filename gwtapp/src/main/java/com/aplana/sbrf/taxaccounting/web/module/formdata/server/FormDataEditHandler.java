package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataEditAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataEditResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormDataEditHandler extends AbstractActionHandler<FormDataEditAction, FormDataEditResult> {

    @Autowired
    private FormDataAccessService accessService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private LogEntryService logEntryService;

    public FormDataEditHandler() {
        super(FormDataEditAction.class);
    }

    @Override
    public FormDataEditResult execute(FormDataEditAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.EDIT_FD;
		TAUserInfo userInfo = securityService.currentUserInfo();
        FormDataEditResult result = new FormDataEditResult();
		FormData formData = action.getFormData();
        Logger logger = new Logger();

        accessService.canEdit(userInfo, formData.getId(), formData.isManual());
        if (formData.isManual()) {
            accessService.canCreateManual(logger, userInfo, formData.getId());
        }

        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(formData.getId());
        if (lockType != null)
            formDataService.locked(formData.getId(), reportType, lockType, logger);

        // http://conf.aplana.com/pages/viewpage.action?pageId=19664668 (2A.1)
        if (!action.isForce() && formDataService.checkExistTask(formData.getId(), formData.isManual(), reportType, logger, userInfo)) {
            result.setUuid(logEntryService.save(logger.getEntries()));
            result.setLockMsg(String.format(LockData.CANCEL_TASKS_MSG, formDataService.getTaskName(reportType, formData.getId(), userInfo)));
            result.setLock(true);
            return result;
        } else {
            formDataService.interruptTask(formData.getId(), formData.isManual(), userInfo.getUser().getId(), reportType);
        }

        return result;
    }

    @Override
    public void undo(FormDataEditAction action, FormDataEditResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
