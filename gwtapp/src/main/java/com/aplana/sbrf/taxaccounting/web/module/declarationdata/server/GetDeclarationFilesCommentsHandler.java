package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationFilesCommentsAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationFilesCommentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetDeclarationFilesCommentsHandler extends AbstractActionHandler<GetDeclarationFilesCommentsAction, GetDeclarationFilesCommentsResult> {

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationDataAccessService accessService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private LogEntryService logEntryService;

    public GetDeclarationFilesCommentsHandler() {
        super(GetDeclarationFilesCommentsAction.class);
    }

    @Override
    @Transactional(readOnly = true)
    public GetDeclarationFilesCommentsResult execute(GetDeclarationFilesCommentsAction action, ExecutionContext executionContext) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetDeclarationFilesCommentsResult result = new GetDeclarationFilesCommentsResult();
        Logger logger = new Logger();
        boolean canEdit = true;
        try {
            accessService.checkEvents(userInfo, action.getDeclarationData().getId(), FormDataEvent.CALCULATE);
        } catch (AccessDeniedException e) {
            canEdit = false;
        }
        if (canEdit) {
            String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationData().getId(), DeclarationDataReportType.EDIT_FILE_COMMENT_DEC);
            LockData lockData = lockService.lock(key, userInfo.getUser().getId(),
                    declarationDataService.getDeclarationFullName(action.getDeclarationData().getId(), DeclarationDataReportType.EDIT_FILE_COMMENT_DEC));
            if (lockData == null) {
                result.setReadOnlyMode(false);
            } else {
                result.setReadOnlyMode(true);
                logger.error("Прикрепление файлов и редактирование комментариев недоступно, так как файлы и комментарии данного экземпляра %s в текущий момент редактируются пользователем \"%s\"",
                        "налоговой формы", taUserService.getUser(lockData.getUserId()).getName());
            }
        } else {
            result.setReadOnlyMode(true);
        }
        result.setFiles(declarationDataService.getFiles(action.getDeclarationData().getId()));
        result.setNote(declarationDataService.getNote(action.getDeclarationData().getId()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(GetDeclarationFilesCommentsAction action, GetDeclarationFilesCommentsResult result, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
