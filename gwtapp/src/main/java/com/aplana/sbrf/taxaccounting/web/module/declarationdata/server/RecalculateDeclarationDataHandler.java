package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RecalculateDeclarationDataHandler extends AbstractActionHandler<RecalculateDeclarationDataAction, RecalculateDeclarationDataResult> {
    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private TAUserService userService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

    public RecalculateDeclarationDataHandler() {
        super(RecalculateDeclarationDataAction.class);
    }

    @Override
    public RecalculateDeclarationDataResult execute(RecalculateDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.XML_DEC;
        TAUserInfo userInfo = securityService.currentUserInfo();
        Map<String, Object> params = new HashMap<String, Object>();
        Logger logger = new Logger();
        try {
            declarationDataService.preCalculationCheck(logger, action.getDeclarationId(), userInfo);
        } catch (Exception e) {
            String uuid;
            if (e instanceof ServiceLoggerException) {
                uuid = ((ServiceLoggerException) e).getUuid();
            } else {
                uuid = logEntryService.save(logger.getEntries());
            }
            throw new ServiceLoggerException("%s. Обнаружены фатальные ошибки", uuid, !TaxType.DEAL.equals(action.getTaxType()) ? "Декларация не может быть сформирована" : "Уведомление не может быть сформировано");
        }
        int userId = userInfo.getUser().getId();
        RecalculateDeclarationDataResult result = new RecalculateDeclarationDataResult();
        try {
            String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), reportType);
            LockData lockDataReportTask = lockDataService.getLock(key);
            if (lockDataReportTask != null && lockDataReportTask.getUserId() == userInfo.getUser().getId()) {
                if (action.isForce()) {
                    // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                    lockDataService.interruptTask(lockDataReportTask, userInfo.getUser().getId(), false);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    String restartMsg = (lockDataReportTask.getState().equals(LockData.State.IN_QUEUE.getText())) ?
                            String.format(LockData.CANCEL_MSG, String.format(ReportType.XML_DEC.getDescription(), action.getTaxType().getDeclarationShortName())) :
                            String.format(LockData.RESTART_MSG, String.format(ReportType.XML_DEC.getDescription(), action.getTaxType().getDeclarationShortName()));
                    result.setRestartMsg(restartMsg);
                    return result;
                }
            } else if (lockDataReportTask != null) {
                try {
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    logger.info(String.format(LockData.LOCK_INFO_MSG,
                            String.format(ReportType.XML_DEC.getDescription(), action.getTaxType().getDeclarationShortName()),
                            sdf.format(lockDataReportTask.getDateLock()),
                            userService.getUser(lockDataReportTask.getUserId()).getName()));
                } catch (ServiceException e) {
                }
                result.setStatus(CreateAsyncTaskStatus.CREATE);
                logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getDeclarationShortName());
                result.setUuid(logEntryService.save(logger.getEntries()));
                return result;
            }
            if (lockDataService.lock(key, userInfo.getUser().getId(),
                    declarationDataService.getDeclarationFullName(action.getDeclarationId(), ReportType.XML_DEC),
                    LockData.State.IN_QUEUE.getText(),
                    lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA)) == null) {
                try {
                    declarationDataService.deleteReport(action.getDeclarationId(), userId, true);
                    params.put("declarationDataId", action.getDeclarationId());
                    params.put("docDate", action.getDocDate());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userId);
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                    LockData lockData = lockDataService.getLock(key);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant);
                    logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getDeclarationShortName());
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } catch (Exception e) {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                    if (e instanceof ServiceLoggerException) {
                        throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                    } else {
                        throw new ActionException(e);
                    }
                }
            } else {
                throw new ActionException("Не удалось запустить формирование отчета. Попробуйте выполнить операцию позже");
            }

        } finally {
            //declarationDataService.unlock(action.getDeclarationId(), userInfo);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(RecalculateDeclarationDataAction action, RecalculateDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
