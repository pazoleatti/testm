package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.model.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class RecalculateDeclarationDataHandler extends AbstractActionHandler<RecalculateDeclarationDataAction, RecalculateDeclarationDataResult> {
    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncManager asyncManager;

    public RecalculateDeclarationDataHandler() {
        super(RecalculateDeclarationDataAction.class);
    }

    @Override
    public RecalculateDeclarationDataResult execute(final RecalculateDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.XML_DEC;
        final RecalculateDeclarationDataResult result = new RecalculateDeclarationDataResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
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
            throw new ServiceLoggerException("%s. Обнаружены фатальные ошибки", uuid, !TaxType.DEAL.equals(action.getTaxType()) ? "Налоговая форма не может быть сформирована" : "Уведомление не может быть сформировано");
        }
        String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ddReportType);
        Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, action.isForce(), logger);
        if (restartStatus != null && restartStatus.getFirst()) {
            result.setStatus(CreateAsyncTaskStatus.LOCKED);
            result.setRestartMsg(restartStatus.getSecond());
        } else if (restartStatus != null && !restartStatus.getFirst()) {
            result.setStatus(CreateAsyncTaskStatus.CREATE);
        } else {
            result.setStatus(CreateAsyncTaskStatus.CREATE);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("declarationDataId", action.getDeclarationId());
            params.put("docDate", action.getDocDate());
            asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, action.isCancelTask(), new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                            declarationDataService.getDeclarationFullName(action.getDeclarationId(), ddReportType));
                }

                @Override
                public void postCheckProcessing() {
                    result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                }

                @Override
                public boolean checkExistTasks(AsyncTaskType reportType, TAUserInfo userInfo, Logger logger) {
                    return declarationDataService.checkExistAsyncTask(action.getDeclarationId(), reportType, logger);
                }

                @Override
                public void interruptTasks(AsyncTaskType reportType, TAUserInfo userInfo) {
                    declarationDataService.interruptAsyncTask(action.getDeclarationId(), userInfo, reportType, TaskInterruptCause.DECLARATION_RECALCULATION);
                }
            });
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(RecalculateDeclarationDataAction action, RecalculateDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
