package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFilesCommentsResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFilesCommentsAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveFilesCommentsHandler extends AbstractActionHandler<SaveFilesCommentsAction, GetFilesCommentsResult> {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public SaveFilesCommentsHandler() {
        super(SaveFilesCommentsAction.class);
    }

    @Override
    @Transactional
    public GetFilesCommentsResult execute(SaveFilesCommentsAction action, ExecutionContext executionContext) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetFilesCommentsResult result = new GetFilesCommentsResult();
        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, action.getFormData().getId(), action.getFormData().isManual(), logger);
        String key = formDataService.generateTaskKey(action.getFormData().getId(), ReportType.EDIT_FILE_COMMENT);
        LockData lockData = lockService.getLock(key);
        if ((formData.getState().equals(WorkflowState.CREATED) || formData.getState().equals(WorkflowState.PREPARED)) &&
                lockData != null && lockData.getUserId() == userInfo.getUser().getId()) {
            result.setReadOnlyMode(false);
            formDataService.saveFilesComments(formData.getId(), action.getNote(), action.getFiles());
            logger.info("Данные успешно сохранены.");
        } else {
            result.setReadOnlyMode(true);
            logger.error("Сохранение не выполнено, так как файлы и комментарии данного экземпляра налоговой формы не заблокированы текущим пользователем.");
        }
        result.setFiles(formDataService.getFiles(formData.getId()));
        result.setNote(formDataService.getNote(formData.getId()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(SaveFilesCommentsAction action, GetFilesCommentsResult result, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
