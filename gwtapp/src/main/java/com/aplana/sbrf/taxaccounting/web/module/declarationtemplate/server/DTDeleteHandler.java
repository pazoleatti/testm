package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DTDeleteAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DTDeleteResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class DTDeleteHandler extends AbstractActionHandler<DTDeleteAction, DTDeleteResult> {

    final static String LOCK_MESSAGE = "Макет \"%s\" заблокирован пользователем с логином \"%s\". Попробуйте выполнить операцию позже.";

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private LockDataService lockDataService;

    public DTDeleteHandler() {
        super(DTDeleteAction.class);
    }

    @Override
    public DTDeleteResult execute(DTDeleteAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        List<String> lockedObjects = new ArrayList<String>();
        Logger logger = new Logger();
        int userId = userInfo.getUser().getId();
        for (DeclarationTemplate template : declarationTemplateService.getDecTemplateVersionsByStatus(action.getDtTypeId())){
            String lockKey = LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + template.getId();
            checkLockAnotherUser(lockDataService.getLock(lockKey), logger,  userInfo.getUser(), template);
            LockData lockData = lockDataService.lock(lockKey, userId, template.getName());
            if (lockData == null) {
                lockedObjects.add(lockKey);
            }
        }

        if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceLoggerException("Ошибка при удалении макета", logEntryService.save(logger.getEntries()));
        }

        try {
            mainOperatingService.deleteTemplate(action.getDtTypeId(), logger, securityService.currentUserInfo());

            DTDeleteResult result = new DTDeleteResult();
            if (!logger.getEntries().isEmpty())
                result.setLogEntriesUuid(logEntryService.save(logger.getEntries()));
            return result;
        } finally {
            for (String lock : lockedObjects) {
                lockDataService.unlock(lock, userId);
            }
        }
    }

    @Override
    public void undo(DTDeleteAction action, DTDeleteResult result, ExecutionContext context) throws ActionException {

    }

    private void checkLockAnotherUser(LockData lockData, Logger logger, TAUser user, DeclarationTemplate template){
        if (lockData != null && lockData.getUserId() != user.getId())
            logger.error(LOCK_MESSAGE, template.getName(), userService.getUser(lockData.getUserId()).getLogin());
    }
}
