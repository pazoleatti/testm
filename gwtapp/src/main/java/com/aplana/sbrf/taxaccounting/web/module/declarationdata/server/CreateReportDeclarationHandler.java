package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportResult;
import com.google.gwt.core.shared.GWT;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhaziev
 *
 */
@Service
public class CreateReportDeclarationHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    public CreateReportDeclarationHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        CreateReportResult result = new CreateReportResult();
        Map<String, Object> params = new HashMap<String, Object>();
        String key = LockData.LOCK_OBJECTS.DECLARATION_DATA.name() + "_" + action.getDeclarationDataId() + "_" + action.getType().getName();
        TAUserInfo userInfo = securityService.currentUserInfo();
        params.put("declarationDataId", action.getDeclarationDataId());
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        Logger logger = new Logger();
        LockData lockData;
        if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 4)) == null) {
            try {
                String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), action.getType());
                if (uuid == null) {
                    params.put(AsyncTask.RequiredParams.LOCK_DATE_END.name(), lockDataService.getLock(key).getDateBefore());
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    asyncManager.executeAsync(action.getType().getAsyncTaskTypeId(isDevelopmentMode()), params, BalancingVariants.SHORT);
                    logger.info(String.format("%s отчет текущей декларации поставлен в очередь на формирование.", action.getType().getName()));
                } else {
                    result.setExistReport(true);
                    lockDataService.unlock(key, userInfo.getUser().getId());
                }
            } catch (Exception e) {
                lockDataService.unlock(key, userInfo.getUser().getId());
                throw new ActionException("Ошибка при постановке в очередь асинхронной задачи", e);
            }
        } else {
            if (lockData.getUserId() != userInfo.getUser().getId()) {
                try {
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                } catch(ServiceException e) {
                }
            }
            logger.info(String.format("%s отчет текущей декларации поставлен в очередь на формирование.", action.getType().getName()));
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

    boolean isDevelopmentMode() {
        return !GWT.isProdMode();
    }

}
