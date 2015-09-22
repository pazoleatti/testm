package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFilesCommentsAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFilesCommentsResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.model.FilesCommentsRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GetFilesCommentsHandler extends AbstractActionHandler<GetFilesCommentsAction, GetFilesCommentsResult> {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private BlobDataService blobDataService;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    public GetFilesCommentsHandler() {
        super(GetFilesCommentsAction.class);
    }

    @Override
    @Transactional
    public GetFilesCommentsResult execute(GetFilesCommentsAction action, ExecutionContext executionContext) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetFilesCommentsResult result = new GetFilesCommentsResult();
        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, action.getFormData().getId(), action.getFormData().isManual(), logger);
        if (formData.getState().equals(WorkflowState.CREATED) || formData.getState().equals(WorkflowState.PREPARED)) {
            String key = formDataService.generateTaskKey(action.getFormData().getId(), ReportType.EDIT_FILE_COMMENT);
            LockData lockData = lockService.lock(key, userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormData().getId(), action.getFormData().isManual(), null, ReportType.EDIT_FILE_COMMENT),
                    lockService.getLockTimeout(LockData.LockObjects.FORM_DATA));
            if (lockData == null) {
                result.setReadOnlyMode(false);
            } else {
                result.setReadOnlyMode(true);
                logger.error("Прикрепление файлов и редактирование комментариев недоступно, так как файлы и комментарии данного экземпляра налоговой формы в текущий момент редактируются пользователем \"%s\"",
                        taUserService.getUser(lockData.getUserId()).getName());
            }
        } else {
            result.setReadOnlyMode(true);
        }
        result.setFiles(formDataService.getFiles(formData.getId()));
        result.setNote(formData.getNote());
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(GetFilesCommentsAction action, GetFilesCommentsResult result, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
