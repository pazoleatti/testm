package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
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
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    public CreateReportHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(final CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        final FormDataReportType fdReportType = FormDataReportType.getFDReportTypeByName(action.getType());
        CreateReportResult result = new CreateReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        String uuid = reportService.get(userInfo, action.getFormDataId(), fdReportType, action.isShowChecked(), action.isManual(), action.isSaved());
        if (uuid != null) {
            result.setExistReport(true);
        } else {
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
            if (lockType == null) {
                String keyTask = formDataService.generateReportKey(action.getFormDataId(), fdReportType, action.isShowChecked(), action.isManual(), action.isSaved());
                Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, formDataService.getTaskName(fdReportType.getReportType(), action.getFormDataId(), userInfo, fdReportType.getName()), userInfo, action.isForce(), logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setLock(true);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setLock(false);
                } else {
                    result.setLock(false);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormDataId());
                    params.put("isShowChecked", action.isShowChecked());
                    params.put("manual", action.isManual());
                    params.put("saved", action.isSaved());
                    if (fdReportType.getReportType().equals(ReportType.SPECIFIC_REPORT))
                        params.put("specificReportType", fdReportType.getName());
                    asyncTaskManagerService.createTask(keyTask, fdReportType.getReportType(), params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                        @Override
                        public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    formDataService.getFormDataFullName(action.getFormDataId(), action.isManual(), null, reportType, fdReportType.getName()),
                                    LockData.State.IN_QUEUE.getText());
                        }

                        @Override
                        public void executePostCheck() {
                        }

                        @Override
                        public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                            return false;
                        }

                        @Override
                        public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                        }

                        @Override
                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                            return formDataService.getTaskName(reportType, action.getFormDataId(), userInfo, fdReportType.getName());
                        }
                    });
                }
            } else {
                formDataService.locked(action.getFormDataId(), fdReportType.getReportType(), lockType, logger, fdReportType.getName());
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
