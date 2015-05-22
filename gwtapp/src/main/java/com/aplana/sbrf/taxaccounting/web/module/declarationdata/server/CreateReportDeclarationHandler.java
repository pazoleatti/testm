package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateReportDeclarationHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private DeclarationDataService declarationDataService;

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
        final ReportType reportType = action.getReportType();
        CreateReportResult result = new CreateReportResult();
        Map<String, Object> params = new HashMap<String, Object>();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        String uuidXml = reportService.getDec(userInfo, action.getDeclarationDataId(), ReportType.XML_DEC);
        if (uuidXml != null) {
            LockData lockData = declarationDataService.lock(action.getDeclarationDataId(), userInfo);
            if (lockData == null) {
                try {
                    String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), reportType);
                    if (uuid != null) {
                        result.setStatus(CreateAsyncTaskStatus.EXIST);
                        return result;
                    } else {
                        String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationDataId(), reportType);
                        LockData lockDataReportTask = lockDataService.getLock(key);
                        if (lockDataReportTask != null && lockDataReportTask.getUserId() == userInfo.getUser().getId()) {
                            if (action.isForce()) {
                                // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                                lockDataService.interruptTask(lockDataReportTask, userInfo.getUser().getId(), false);
                            } else {
                                result.setStatus(CreateAsyncTaskStatus.LOCKED);
                                return result;
                            }
                        } else if (lockDataReportTask != null) {
                            try {
                                lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                            } catch (ServiceException e) {
                            }
                            result.setStatus(CreateAsyncTaskStatus.CREATE);
                            logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getDeclarationShortName());
                            result.setUuid(logEntryService.save(logger.getEntries()));
                            return result;
                        }
                        if (lockDataService.lock(key, userInfo.getUser().getId(),
                                declarationDataService.getDeclarationFullName(action.getDeclarationDataId(), reportType),
                                LockData.State.IN_QUEUE.getText(),
                                lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA)) == null) {
                            try {
                                params.put("declarationDataId", action.getDeclarationDataId());
                                params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                                lockData = lockDataService.getLock(key);
                                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                                lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                                BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                                lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant.getName());
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
                    }
                } finally {
                    declarationDataService.unlock(action.getDeclarationDataId(), userInfo);
                }
            } else {
                throw new ActionException("Декларация заблокирована и не может быть изменена. Попробуйте выполнить операцию позже");
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
