package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

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
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreatePdfReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreatePdfReportResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreatePdfDeclarationHandler extends AbstractActionHandler<CreatePdfReportAction, CreatePdfReportResult> {

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

    public CreatePdfDeclarationHandler() {
        super(CreatePdfReportAction.class);
    }

    @Override
    public CreatePdfReportResult execute(CreatePdfReportAction action, ExecutionContext executionContext) throws ActionException {
        CreatePdfReportResult result = new CreatePdfReportResult();
        Map<String, Object> params = new HashMap<String, Object>();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        String uuidXml = reportService.getDec(userInfo, action.getDeclarationDataId(), ReportType.XML_DEC);
        if (uuidXml != null) {
            LockData lockData = declarationDataService.lock(action.getDeclarationDataId(), userInfo);
            if (lockData == null) {
                try {
                    result.setExistReportXml(true);
                    String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), ReportType.PDF_DEC);
                    if (uuid != null) {
                        return result;
                    } else {
                        try {
                            String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationDataId(), ReportType.PDF_DEC);
                            LockData lockDataReportTask = lockDataService.getLock(key);
                            if (lockDataReportTask != null && lockDataReportTask.getUserId() == userInfo.getUser().getId()) {
                                if (action.isForce()) {
                                    // ToDo Оправляем оповещение подписавщимся пользователям, удаляем старую задачу,
                                    List<Integer> users = lockDataService.getUsersWaitingForLock(key);
                                    lockDataService.unlock(key, userInfo.getUser().getId(), true);
                                } else {
                                    result.setExistTask(true);
                                    return result;
                                }
                            } else {
                                try {
                                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                                } catch (ServiceException e) {
                                }
                                logger.info("Пользователь подписан на получение оповещений по выполняющейся задаче на формирование формы предварительного просмотра");
                                return result;
                            }
                            if (lockDataService.lock(key, userInfo.getUser().getId(),
                                    lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA)) == null) {
                                try {
                                    params.put("declarationDataId", action.getDeclarationDataId());
                                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockDataService.getLock(key).getDateLock());
                                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                                    asyncManager.executeAsync(ReportType.PDF_DEC.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                                    logger.info(String.format("%s отчет текущей декларации поставлен в очередь на формирование.", ReportType.PDF_DEC.getName()));
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
                            declarationDataService.unlock(action.getDeclarationDataId(), userInfo);
                        }
                    }
                } finally {
                    declarationDataService.unlock(action.getDeclarationDataId(), userInfo);
                }
            } else {
                throw new ActionException("Декларация заблокирована и не может быть изменена. Попробуйте выполнить операцию позже");
            }
        } else {
            result.setExistReportXml(false);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreatePdfReportAction searchAction, CreatePdfReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
