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
        LockData lockData = declarationDataService.lock(action.getDeclarationDataId(), userInfo);
        if (lockData == null) {
            try {
                String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationDataId(), ReportType.PDF_DEC);
                params.put("declarationDataId", action.getDeclarationDataId());
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                LockData lockDataReportTask = lockDataService.getLock(key);
                if (lockDataReportTask != null && (action.isForce() != null && action.isForce())) {
                    // Удаляем старую задачу
                    List<Integer> users = lockDataService.getUsersWaitingForLock(key);
                    lockDataService.unlock(key, userInfo.getUser().getId(), true);
                } else if (action.isForce() != null && action.isForce() == false) {
                    if (lockDataReportTask.getUserId() != userInfo.getUser().getId()) {
                        try {
                            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                        } catch (ServiceException e) {
                        }
                    }
                    logger.info("Пользователь подписан на получение оповещений по выполняющейся задаче на формирование формы предварительного просмотра");
                }
                lockDataReportTask = lockDataService.lock(key, userInfo.getUser().getId(),
                        declarationDataService.getDeclarationFullName(action.getDeclarationDataId(), "PDF"),
                        LockData.State.IN_QUEUE.getText(),
                        lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA));
                if (lockDataReportTask == null) {
                    try {
                        String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), ReportType.PDF_DEC);
                        if ((action.isForce() != null && action.isForce()) || uuid == null) { // || !action.isExistPdf()
                            if (uuid != null) {
                                reportService.deleteDec(reportService.getDec(userInfo, action.getDeclarationDataId(), ReportType.JASPER_DEC));
                                reportService.deleteDec(uuid);
                            }

                            lockData = lockDataService.getLock(key);
                            params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                            BalancingVariants balancingVariant = asyncManager.executeAsync(ReportType.PDF_DEC.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                            lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant.getName());
                            logger.info(String.format("%s отчет текущей декларации поставлен в очередь на формирование.", ReportType.PDF_DEC.getName()));
                        } else {
                            result.setExistReport(true);
                            lockDataService.unlock(key, userInfo.getUser().getId());
                        }
                    } catch (Exception e) {
                        lockDataService.unlock(key, userInfo.getUser().getId());
                        if (e instanceof ServiceLoggerException) {
                            throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                        } else {
                            throw new ActionException(e);
                        }
                    }
                } else {
                    result.setExistTask(true);
                }
            } finally {
                declarationDataService.unlock(action.getDeclarationDataId(), userInfo);
            }
        } else {
            throw new ActionException("Декларация заблокирована и не может быть изменена. Попробуйте выполнить операцию позже");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreatePdfReportAction searchAction, CreatePdfReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
