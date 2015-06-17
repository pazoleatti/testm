package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CreateReportResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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

    @Autowired
    private TAUserService userService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

    public CreateReportHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        final ReportType reportType = action.getType();
        CreateReportResult result = new CreateReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        String uuid = reportService.get(userInfo, action.getFormDataId(), reportType, action.isShowChecked(), action.isManual(), action.isSaved());
        if (uuid != null) {
            result.setExistReport(true);
        } else {
            String key = LockData.LockObjects.FORM_DATA.name() + "_" + action.getFormDataId() + "_" + reportType.getName() + "_isShowChecked_" + action.isShowChecked() + "_manual_" + action.isManual() + "_saved_" + action.isSaved();
            LockData lockDataTask = lockDataService.getLock(key);
            if (lockDataTask != null && lockDataTask.getUserId() == userInfo.getUser().getId()) {
                if (action.isForce()) {
                    // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                    lockDataService.interruptTask(lockDataTask, userInfo.getUser().getId(), false);
                } else {
                    result.setLock(true);
                    String restartMsg = (lockDataTask.getState().equals(LockData.State.IN_QUEUE.getText())) ?
                            String.format(LockData.CANCEL_MSG, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo)) :
                            String.format(LockData.RESTART_MSG, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo));
                    result.setRestartMsg(restartMsg);
                    return result;
                }
            } else if (lockDataTask != null) {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormDataId());
                    params.put("isShowChecked", action.isShowChecked());
                    params.put("manual", action.isManual());
                    params.put("saved", action.isSaved());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);                        lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    logger.info(String.format(LockData.LOCK_INFO_MSG,
                            String.format(reportType.getDescription(), action.isManual() ? "версия ручного ввода" : "автоматическая версия"),
                            sdf.format(lockDataTask.getDateLock()),
                            userService.getUser(lockDataTask.getUserId()).getName()));
                } catch (ServiceException e) {
                }
                result.setLock(false);
                logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.isManual() ? "версия ручного ввода" : "автоматическая версия");
                result.setUuid(logEntryService.save(logger.getEntries()));
                return result;
            }
            LockData lockData;
            if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormDataId(), null, reportType),
                    LockData.State.IN_QUEUE.getText(),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA))) == null) {
                try {
                    lockData = lockDataService.getLock(key);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormDataId());
                    params.put("isShowChecked", action.isShowChecked());
                    params.put("manual", action.isManual());
                    params.put("saved", action.isSaved());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant);
                    logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.isManual() ? "версия ручного ввода" : "автоматическая версия");
                } catch (Exception e) {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                    int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
                    if (i != -1) {
                        throw (ServiceLoggerException) ExceptionUtils.getThrowableList(e).get(i);
                    }
                    throw new ActionException(e);
                }
            } else {
                if (lockData.getUserId() != userInfo.getUser().getId()) {
                    try {
                        lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    } catch (ServiceException e) {
                    }
                }
                logger.info(String.format("%s отчет текущей налоговой формы (%s) поставлен в очередь на формирование.", reportType.getName(), action.isManual() ? "версия ручного ввода" : "автоматическая версия"));
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
