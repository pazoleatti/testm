package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
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
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AcceptDeclarationDataHandler extends AbstractActionHandler<AcceptDeclarationDataAction, AcceptDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    public AcceptDeclarationDataHandler() {
        super(AcceptDeclarationDataAction.class);
    }

    @Override
    public AcceptDeclarationDataResult execute(final AcceptDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.ACCEPT_DEC;
        final AcceptDeclarationDataResult result = new AcceptDeclarationDataResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        if (action.isAccepted()) {
            String uuidXml = reportService.getDec(userInfo, action.getDeclarationId(), DeclarationDataReportType.XML_DEC);
            if (uuidXml != null) {
                DeclarationData declarationData = declarationDataService.get(action.getDeclarationId(), userInfo);
                if (!declarationData.isAccepted()) {
                    String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ddReportType);
                    Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(ddReportType, action.getTaxType()), userInfo, action.isForce(), logger);
                    if (restartStatus != null && restartStatus.getFirst()) {
                        result.setStatus(CreateAsyncTaskStatus.LOCKED);
                        result.setRestartMsg(restartStatus.getSecond());
                    } else if (restartStatus != null && !restartStatus.getFirst()) {
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                    } else {
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("declarationDataId", action.getDeclarationId());
                        asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, action.isCancelTask(), PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                            @Override
                            public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                        declarationDataService.getDeclarationFullName(action.getDeclarationId(), ddReportType),
                                        LockData.State.IN_QUEUE.getText());
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
                                declarationDataService.interruptTask(action.getDeclarationId(), userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                            }

                            @Override
                            public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                return declarationDataService.getTaskName(ddReportType, action.getTaxType());
                            }
                        });
                    }
                } else {
                    result.setStatus(CreateAsyncTaskStatus.EXIST);
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            declarationDataService.cancel(logger, action.getDeclarationId(), securityService.currentUserInfo());
            result.setStatus(CreateAsyncTaskStatus.EXIST);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(AcceptDeclarationDataAction action, AcceptDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
