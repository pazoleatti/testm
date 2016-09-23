package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ConsolidateAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ConsolidateResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class ConsolidateHandler extends AbstractActionHandler<ConsolidateAction, ConsolidateResult> {

    @Autowired
    private FormDataService formDataService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    public ConsolidateHandler() {
        super(ConsolidateAction.class);
    }

    @Override
    public ConsolidateResult execute(final ConsolidateAction action, ExecutionContext executionContext) throws ActionException {
        final ReportType reportType = ReportType.CONSOLIDATE_FD;
        final ConsolidateResult result = new ConsolidateResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        if (lockType == null || !ReportType.EDIT_FD.equals(lockType.getFirst())) {
            String keyTask = formDataService.generateTaskKey(action.getFormDataId(), reportType);
            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo), userInfo, action.isForce(), logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setLock(true);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setLock(false);
            } else {
                result.setLock(false);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("formDataId", action.getFormDataId());
                asyncTaskManagerService.createTask(keyTask, reportType, params, action.isCancelTask(), PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                    @Override
                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                formDataService.getFormDataFullName(action.getFormDataId(), action.isManual(), null, reportType),
                                LockData.State.IN_QUEUE.getText());
                    }

                    @Override
                    public void executePostCheck() {
                        result.setLockTask(true);
                    }

                    @Override
                    public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                        return formDataService.checkExistTask(action.getFormDataId(), false, reportType, logger, userInfo);
                    }

                    @Override
                    public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                        formDataService.interruptTask(action.getFormDataId(), false, userInfo, reportType, TaskInterruptCause.FORM_CONSOLIDATION);
                    }

                    @Override
                    public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                        return formDataService.getTaskName(reportType, action.getFormDataId(), userInfo);
                    }
                });
                if (!result.isLockTask())
                    formDataService.checkSources(action.getFormDataId(), action.isManual(), userInfo, logger);
            }
        } else {
            formDataService.locked(action.getFormDataId(), reportType, lockType, logger);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(ConsolidateAction consolidateAction, ConsolidateResult consolidateResult, ExecutionContext executionContext) throws ActionException {

    }
}
