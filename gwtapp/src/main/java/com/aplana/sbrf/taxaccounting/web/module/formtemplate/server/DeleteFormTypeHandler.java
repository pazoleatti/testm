package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.DeleteFormTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.DeleteFormTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class DeleteFormTypeHandler extends AbstractActionHandler<DeleteFormTypeAction, DeleteFormTypeResult> {

    final static String LOCK_MESSAGE = "Макет \"%s\" заблокирован пользователем с логином \"%s\". Попробуйте выполнить операцию позже.";

    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    private MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormTemplateDao formTemplateDao;

    @Autowired
    private LockDataService lockDataService;

    public DeleteFormTypeHandler() {
        super(DeleteFormTypeAction.class);
    }

    @Override
    public DeleteFormTypeResult execute(DeleteFormTypeAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        List<String> lockedObjects = new ArrayList<String>();
        Logger logger = new Logger();
        int userId = userInfo.getUser().getId();
        for (FormTemplate template : formTemplateService.getFormTemplateVersionsByStatus(action.getFormTypeId())){
            String lockKey = LockData.LockObjects.FORM_TEMPLATE.name() + "_" + template.getId();
            checkLockAnotherUser(lockDataService.getLock(lockKey), logger,  userInfo.getUser(), template);
            LockData lockData = lockDataService.lock(lockKey, userId, template.getName());
            if (lockData == null) {
                lockedObjects.add(lockKey);
                try {
                    //Блокировка установлена
                    lockedObjects.add(lockKey);
                } finally {
                    for (String lock : lockedObjects) {
                        lockDataService.unlock(lock, userId);
                    }
                }
            }
        }

        try{
            DeleteFormTypeResult result = new DeleteFormTypeResult();
            mainOperatingService.deleteTemplate(action.getFormTypeId(), logger, securityService.currentUserInfo());

            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        } finally {
            for (String lock : lockedObjects) {
                lockDataService.unlock(lock, userId);
            }
        }
    }

    @Override
    public void undo(DeleteFormTypeAction action, DeleteFormTypeResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }

    private void checkLockAnotherUser(LockData lockData, Logger logger, TAUser user, FormTemplate template){
        if (lockData != null && lockData.getUserId() != user.getId())
            logger.error(LOCK_MESSAGE, template.getName(), userService.getUser(lockData.getUserId()).getLogin());
    }
}
