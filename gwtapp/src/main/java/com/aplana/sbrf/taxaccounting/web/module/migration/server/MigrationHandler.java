package com.aplana.sbrf.taxaccounting.web.module.migration.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.MessageService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationAction;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class MigrationHandler extends AbstractActionHandler<MigrationAction, MigrationResult> {

    // EJB-модуль отправки JMS-сообщений
    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private SecurityService securityService;
    @Autowired
    private LockDataService lockDataService;
    // девелоперская отладка
//    @Autowired
//    private MigrationService migrationService;

    public MigrationHandler() {
        super(MigrationAction.class);
    }

    @Override
    public MigrationResult execute(MigrationAction action, ExecutionContext executionContext)
            throws ActionException {
        // Отправка файлов
        MigrationResult result = new MigrationResult();
        Map<String, Object> params = new HashMap<String, Object>();
        String key = "TEST_ASYNC";
        TAUserInfo userInfo = securityService.currentUserInfo();
        params.put("text", "asdasd");
        params.put("digits", 123);
        params.put("date", new Date());
        params.put("userInfo", userInfo);
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        try {
            System.out.println("userId: " +userInfo.getUser().getId());
            lockDataService.lock(key, userInfo.getUser().getId(), 5000);
            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
            asyncManager.executeAsync(2L, params, BalancingVariants.SHORT);
        } catch (AsyncTaskException e) {
            String msg = "Ошибка отправки транспортных файлов JMS-сообщениями.";
            throw new ActionException(msg, e);
        }
        return result;
    }

    @Override
    public void undo(MigrationAction action, MigrationResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
