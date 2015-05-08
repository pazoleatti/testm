package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CreateReportResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
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
public class CreateReportHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {

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

    @Autowired
    private FormDataService formDataService;

    public CreateReportHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        CreateReportResult result = new CreateReportResult();
        Map<String, Object> params = new HashMap<String, Object>();
        String key = LockData.LockObjects.FORM_DATA.name() + "_" + action.getFormDataId() + "_" + action.getType().getName() + "_isShowChecked_" + action.isShowChecked() + "_manual_" + action.isManual() + "_saved_" + action.isSaved();
        TAUserInfo userInfo = securityService.currentUserInfo();
        params.put("formDataId", action.getFormDataId());
        params.put("isShowChecked", action.isShowChecked());
        params.put("manual", action.isManual());
        params.put("saved", action.isSaved());
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        Logger logger = new Logger();
        LockData lockData;
        if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                formDataService.getFormDataFullName(action.getFormDataId(), null, action.getType().getName()),
                LockData.State.IN_QUEUE.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA))) == null) {
            try {
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockDataService.getLock(key).getDateLock());
                String uuid = reportService.get(userInfo, action.getFormDataId(), action.getType(), action.isShowChecked(), action.isManual(), action.isSaved());
                if (uuid == null) {
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    asyncManager.executeAsync(action.getType().getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    logger.info(String.format("%s отчет текущей налоговой формы (%s) поставлен в очередь на формирование.", action.getType().getName(), action.isManual()?"версия ручного ввода":"автоматическая версия"));
                } else {
                    result.setExistReport(true);
                    lockDataService.unlock(key, userInfo.getUser().getId());
                }
            } catch (Exception e) {
                try {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                } catch (ServiceException e2) {
                    if (PropertyLoader.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутсвии блокировки, если оня снята при выбрасывании исключения
                        throw new ActionException(e2);
                    }
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ActionException(e);
                }
            }
        } else {
            if (lockData.getUserId() != userInfo.getUser().getId()) {
                try {
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                } catch(ServiceException e) {
                }
            }
            logger.info(String.format("%s отчет текущей налоговой формы (%s) поставлен в очередь на формирование.", action.getType().getName(), action.isManual() ? "версия ручного ввода" : "автоматическая версия"));
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
