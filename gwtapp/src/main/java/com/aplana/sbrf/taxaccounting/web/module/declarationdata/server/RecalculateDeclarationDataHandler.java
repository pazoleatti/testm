package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
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
    private LockDataService lockDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    public RecalculateDeclarationDataHandler() {
        super(RecalculateDeclarationDataAction.class);
    }

    @Override
    public RecalculateDeclarationDataResult execute(final RecalculateDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.XML_DEC;
        final RecalculateDeclarationDataResult result = new RecalculateDeclarationDataResult();
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
            throw new ServiceLoggerException("%s. Обнаружены фатальные ошибки", uuid, !TaxType.DEAL.equals(action.getTaxType()) ? "Декларация не может быть сформирована" : "Уведомление не может быть сформировано");
        }
        String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), reportType);
        Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, String.format(reportType.getDescription(), action.getTaxType().getDeclarationShortName()), userInfo, action.isForce(), logger);
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
            asyncTaskManagerService.createTask(keyTask, reportType, params, action.isCancelTask(), PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                @Override
                public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                    return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                            declarationDataService.getDeclarationFullName(action.getDeclarationId(), reportType),
                            LockData.State.IN_QUEUE.getText(),
                            lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA));
                }

                @Override
                public void executePostCheck() {
                    result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                }

                @Override
                public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                    return declarationDataService.checkExistTask(action.getDeclarationId(), reportType, logger);
                }

                @Override
                public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                    declarationDataService.interruptTask(action.getDeclarationId(), userInfo.getUser().getId(), reportType);
                }

                @Override
                public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                    return String.format(reportType.getDescription(), action.getTaxType().getDeclarationShortName());
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
