package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationFilesCommentsResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SaveDeclarationFilesCommentsAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class SaveDeclarationFilesCommentsHandler extends AbstractActionHandler<SaveDeclarationFilesCommentsAction, GetDeclarationFilesCommentsResult> {

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationDataAccessService accessService;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public SaveDeclarationFilesCommentsHandler() {
        super(SaveDeclarationFilesCommentsAction.class);
    }

    @Override
    @Transactional
    public GetDeclarationFilesCommentsResult execute(SaveDeclarationFilesCommentsAction action, ExecutionContext executionContext) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetDeclarationFilesCommentsResult result = new GetDeclarationFilesCommentsResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationData().getId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationData().getId());
            return result;
        }
        Logger logger = new Logger();
        String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationData().getId(), DeclarationDataReportType.EDIT_FILE_COMMENT_DEC);
        LockData lockData = lockService.getLock(key);
        if (lockData != null && lockData.getUserId() == userInfo.getUser().getId()) {
            try {
                accessService.checkEvents(userInfo, action.getDeclarationData().getId(), FormDataEvent.CALCULATE);
            } catch (AccessDeniedException e) {
                //удаляем блокировку, если пользователю недоступно редактирование
                lockService.unlock(key, userInfo.getUser().getId());
                throw e;
            }
            result.setReadOnlyMode(false);
            declarationDataService.saveFilesComments(action.getDeclarationData().getId(), action.getNote(), action.getFiles());
            logger.info("Данные успешно сохранены.");
        } else {
            result.setReadOnlyMode(true);
            logger.error("Сохранение не выполнено, так как файлы и комментарии данного экземпляра %s не заблокированы текущим пользователем.",
                    "налоговой формы");
        }
        result.setFiles(declarationDataService.getFiles(action.getDeclarationData().getId()));
        result.setNote(declarationDataService.getNote(action.getDeclarationData().getId()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(SaveDeclarationFilesCommentsAction action, GetDeclarationFilesCommentsResult result, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
